#!/usr/bin/env ruby

require 'curb'
require_relative 'constants'

unless File.exist? DATA_DIR
  Dir.mkdir DATA_DIR
end

unless File.exist? OBSESSIONS_DIR
  Dir.mkdir OBSESSIONS_DIR
end

http = Curl.get(OBSESSIONS_URI)
open(OBSESSIONS_HTML_FILE, 'w') do |f|
  f.puts http.body_str
end
