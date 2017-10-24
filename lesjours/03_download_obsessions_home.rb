#!/usr/bin/env ruby

require 'curb'
require 'json'

require_relative 'constants'

obsessions = JSON.parse(IO.read(OBSESSIONS_JSON_FILE))
obsessions.each_with_index do |obsession, index|
  p "Fetching #{index} [#{obsession['href']}]"
  obsession_dir = File.join(OBSESSIONS_DIR, index.to_s)
  unless File.exist? obsession_dir
    Dir.mkdir obsession_dir
  end
  http = Curl.get(obsession['href'])
  open(File.join(obsession_dir, 'index.html'), 'w') do |f|
    f.puts http.body_str
  end  
end
