#!/usr/bin/env ruby

require 'nokogiri'
require 'json'
require 'addressable'

require_relative 'constants'

doc = File.open(OBSESSIONS_HTML_FILE) { |f| Nokogiri::HTML(f) }
obsessions = []

doc.css('.card-obsession').each do |obsession|
  obsessions << {
    'href': Addressable::URI.join(OBSESSIONS_URI, obsession['href']),
    'name': obsession.css('.responsive-card')[0]['alt']
  }
end

File.open(OBSESSIONS_JSON_FILE, 'w') do |f|
  f.write(JSON.pretty_generate(obsessions))
end
