#!/usr/bin/env ruby

require 'curb'
require 'json'

require_relative 'constants'

unless File.exist? OBSESSIONS_DIR
  Dir.mkdir OBSESSIONS_DIR
end

obsessions = JSON.parse(IO.read(OBSESSIONS_WITH_EPISODES_JSON_FILE))
obsessions.each_with_index do |obsession, index_obsession|
  p "Processing obsession #{index_obsession}"
  obsession['articles'].each_with_index do |episode, index_episode|
    episode_dir = File.join(OBSESSIONS_DIR, index_obsession.to_s, index_episode.to_s)
    unless File.exist? episode_dir
      Dir.mkdir episode_dir
    end
    target_file = File.join(episode_dir, 'index.html')
    unless File.exist? target_file
      p "Processing episode #{index_obsession}-#{index_episode} [#{episode['href']}] to [#{episode_dir}]"
      http = Curl.get(episode['href']) do |http|
        http.headers['Cookie'] = COOKIE_VALUE
      end
      open(target_file, 'w') do |f|
        f.puts http.body_str
      end
      sleep(2)
    end
  end
end
