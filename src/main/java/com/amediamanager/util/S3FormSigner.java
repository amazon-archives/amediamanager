/*
 * Copyright 2014 Amazon Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *    http://aws.amazon.com/apache2.0
 *
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and
 * limitations under the License.
 */
package com.amediamanager.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;

/**
 * The AWSS3Signer class is a helper class create to assist with the generation of pre-signed URLs
 * for S3 upload forms.  The AmazonS3Client.generatePresignedUrl method cannot be used in this case
 * because it does not have an option for signing S3 policies, which are required for form-based upload.
 * More on the S3 form upload approach can be read here:
 * http://docs.amazonwebservices.com/AmazonS3/latest/dev/index.html?UsingHTTPPOST.html
 *
 */
public abstract class S3FormSigner {
    private static final Logger LOG = LoggerFactory.getLogger(S3FormSigner.class);

    /**
     * The SignRequest method takes a set of AWS credentials and the S3 upload policy string and returns the encoded policy and the signature.
     *
     * @param creds        the AWS credentials to be used for signing the request
     * @param policy    the policy file to applied to the upload
     * @return            an array of strings containing the base 64 encoded policy (index 0) and the signature (index 1).
     */
     String[] signRequest(AWSCredentialsProvider credsProvider, String policy) {

        String[] policyAndSignature = new String[2];

        try{
            // Create a Base64 encoded version of the policy string for placement in the form and
            // for use in signature generation.  Returns are stripped out from the policy string.
            String encodedPolicy = new String(Base64.encodeBase64(
                    policy.replaceAll("\n","").replaceAll("\r","")
                    .getBytes("UTF-8")));

            // AWS signatures are generated using SHA1 HMAC signing.
            Mac hmac = Mac.getInstance("HmacSHA1");

            // Generate the signature using the Secret Key from the AWS credentials
            hmac.init(new SecretKeySpec(credsProvider.getCredentials().getAWSSecretKey().getBytes("UTF-8"), "HmacSHA1"));

            String signature = new String(Base64.encodeBase64(
                    hmac.doFinal(encodedPolicy.getBytes("UTF-8"))));

            // Pack the encoded policy and the signature into a string array
            policyAndSignature[0] = encodedPolicy;
            policyAndSignature[1] = signature;

        } catch (UnsupportedEncodingException e) {
            LOG.error("Unsupport encoding", e);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("No such algorithm", e);
        } catch (InvalidKeyException e) {
            LOG.error("Invalid key", e);
        }

        return policyAndSignature;
    }

    /**
     * The UploadPolicy method creates the S3 upload policy for the aMediaManager application.
     * Much of this is hard coded and would have to change with any changes to the fields in the S3
     * upload form.
     *
     * @param key            this is not currently used.
     * @param redirectUrl    this is the URL to which S3 will redirect the browser on successful upload.
     * @return                the upload policy string is returned.
     */
     String generateUploadPolicy(String s3BucketName, String keyPrefix, AWSCredentialsProvider credsProvider, String redirectUrl) {

        Calendar dateTime = Calendar.getInstance();
        // add the offset from UTC
        dateTime.add(Calendar.MILLISECOND, -dateTime.getTimeZone().getOffset(dateTime.getTimeInMillis()));
        // add 15 minutes more for skew
        dateTime.add(Calendar.MINUTE, 15);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        String expirationDate = dateFormatter.format(dateTime.getTime());


        StringBuilder sb = new StringBuilder();
        sb.append("{ \"expiration\": \"" + expirationDate + "\",");
        sb.append("\"conditions\": [ { \"bucket\": \"" + s3BucketName  + "\" }, ");
        sb.append("[\"starts-with\", \"$key\", \"" + keyPrefix + "/\"], ");
        sb.append("{ \"success_action_redirect\": \"" + redirectUrl + "\" },");
        sb.append("[\"eq\", \"$x-amz-meta-bucket\", \"" + s3BucketName + "\"], ");
        sb.append("[\"starts-with\", \"$x-amz-meta-owner\", \"\"], ");
        sb.append("[\"starts-with\", \"$x-amz-meta-uuid\", \"\"], ");
        sb.append("[\"starts-with\", \"$x-amz-meta-title\", \"\"], ");
        sb.append("[\"starts-with\", \"$x-amz-meta-tags\", \"\"], ");
        sb.append("[\"starts-with\", \"$x-amz-meta-createdDate\", \"\"], ");
        sb.append("[\"starts-with\", \"$x-amz-meta-description\", \"\"], ");
        sb.append("[\"starts-with\", \"$x-amz-meta-privacy\", \"\"], ");
        sb.append("[\"starts-with\", \"$Content-Type\", \"video/\"], ");

        if(credsProvider.getCredentials() instanceof BasicSessionCredentials) {
            sb.append("[\"starts-with\", \"$x-amz-security-token\", \"\"], ");
        }

        sb.append("[\"content-length-range\", 0, 1073741824] ] }");
        return sb.toString();
    }
}
