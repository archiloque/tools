#!/usr/bin/env ruby

require 'addressable'
require 'curb'
require 'json'
require 'nokogiri'
require_relative 'constants'

unless File.exist? DATA_DIR
  Dir.mkdir DATA_DIR
end

http = Curl.get('http://digitalhumanities.org/dhq/')
document = Nokogiri::HTML(http.body_str)
issues = {}
document.css('#leftsidenav a').each do |issue|
  href = issue['href']
  if href.start_with? '/dhq/vol/'
    issues[issue.text] = Addressable::URI.join('http://digitalhumanities.org/dhq/', href)
  end
end

File.open(ISSUES_JSON_FILE, 'w') do |f|
  f.write(JSON.pretty_generate(issues))
end
