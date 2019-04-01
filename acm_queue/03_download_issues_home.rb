#!/usr/bin/env ruby

require 'curb'
require 'json'

require_relative 'constants'

unless File.exist? ISSUES_DIR
  Dir.mkdir ISSUES_DIR
end

issues = JSON.parse(IO.read(ISSUES_JSON_FILE))
issues.each_with_index do |issue, index|
  p "Fetching #{index} [#{issue['href']}]"
  issues_dir = File.join(ISSUES_DIR, index.to_s)
  unless File.exist? issues_dir
    Dir.mkdir issues_dir
  end
  Curl::Easy.download(issue['href'], File.join(issues_dir, 'index.html'))
end
