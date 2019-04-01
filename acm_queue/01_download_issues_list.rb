#!/usr/bin/env ruby

require 'curb'
require_relative 'constants'

unless File.exist? DATA_DIR
  Dir.mkdir DATA_DIR
end

Curl::Easy.download(MAIN_URI, MAIN_HTML_FILE)
