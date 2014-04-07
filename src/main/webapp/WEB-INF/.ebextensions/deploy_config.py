#!/bin/env python
"""
This script attempts to load configuration found in the OS environment into S3.
It checks S3 to see if the configuration file already exists and exits if so.
If the config filed does not exist, the script scans the OS environ for vars
with a specific prefix, merges them into a key=val ini file and uploads
the file to S3.

The script assumes the existence of S3_CONFIG_BUCKET and S3_CONFIG_KEY env vars
to point to the S3 bucket and key that should hold the configuration.
"""
import os
import sys

import boto
from boto.s3.key import Key

# Prefix for env vars that hold config
ENV_VAR_PREFIX = 'AMM_'

def get_conf():

  # Env vars that hold pointers
  config_bucket = os.environ['S3_CONFIG_BUCKET']
  config_key = os.environ['S3_CONFIG_KEY']
	
  config = []
  
  # Look for configuration in S3
  s3 = boto.connect_s3()
  cfg_bucket = s3.lookup(config_bucket)
  
  # Fail if bucket doesn't exist
  if cfg_bucket is None:
      print "Could not find bucket " + config_bucket
      sys.exit(-1)
      
  # Bucket exists; look for config file in bucket
  if(cfg_bucket.get_key(config_key)):
    print "Config file found at s3://%s/%s. Exiting\n" % (config_bucket, config_key)
    sys.exit(0)
    
  # Retrieve env vars with AMM_ prefix
  print "Config file not found in S3"
  print "Scanning env for config vars prefixed with " + ENV_VAR_PREFIX
  for var in os.environ:
      if str(var).startswith(ENV_VAR_PREFIX):
          config.append("%s=%s" % (var.replace(ENV_VAR_PREFIX, ''), os.environ[var]))
  config_string = "\n".join(config)
  print "Got config:\n" + config_string
  
  # Store config in S3
  k = Key(cfg_bucket)
  k.key = config_key
  k.set_contents_from_string(config_string)
  print "Config file stored at s3://%s/%s. Exiting\n" % (config_bucket, config_key)
  sys.exit(0)
    
if __name__ == "__main__":
  if os.environ.get('DO_S3_CONFIG'):
    get_conf()