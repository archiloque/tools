#!/usr/bin/env ruby

require 'nokogiri'
require 'json'
require 'addressable'

require_relative 'constants'

obsessions = JSON.parse(IO.read(OBSESSIONS_JSON_FILE))
obsessions.each_with_index do |obsession, index|
  obsession_file = File.join(OBSESSIONS_DIR, index.to_s, 'index.html')
  p "Reading #{index} [#{obsession_file}]"
  doc = File.open(obsession_file) { |f| Nokogiri::HTML(f) }
  obsession['articles'] = doc.css('#episodes').css('.article').map do |article|
    inner_link = article.css('.article-content').css('a')[0]
    {
      'href': Addressable::URI.join(obsession['href'], inner_link['href']),
      'name': inner_link.css('h2')[0].inner_text.strip,
    }
  end
end

File.open(OBSESSIONS_WITH_EPISODES_JSON_FILE, 'w') do |f|
  f.write(JSON.pretty_generate(obsessions))
end
