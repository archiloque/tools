#!/usr/bin/env ruby

require 'curb'
require 'json'
require 'nokogiri'
require 'addressable'

require_relative 'constants'

obsessions = JSON.parse(IO.read(OBSESSIONS_WITH_EPISODES_JSON_FILE))
obsessions.each_with_index do |obsession, index_obsession|
  p "Processing obsession #{index_obsession} [#{obsession['href']}]"
  obsession['articles'].each_with_index do |episode, index_episode|
    episode_dir = File.join(OBSESSIONS_DIR, index_obsession.to_s, index_episode.to_s)
    source_episode_file = File.join(episode_dir, 'index.html')
    if File.exist? source_episode_file
      episode_images_infos = File.join(episode_dir, 'images.json')
      unless File.exist? episode_images_infos
        p "Processing episode #{index_obsession}-#{index_episode} [#{episode['href']}]"
        doc = File.open(source_episode_file) { |f| Nokogiri::HTML(f) }

        article_image_url = doc.at_xpath('//meta[@property="og:image"]/@content').text
        article_image_url = Addressable::URI.join(episode['href'], article_image_url).to_s
        logo_image_extension = Addressable::URI.parse(article_image_url).extname
        
        http = Curl.get(article_image_url)
        open(File.join(episode_dir, "logo#{logo_image_extension}"), 'w') do |f|
          f.puts http.body_str
        end
        sleep(1)
        
        images = {}
        doc.css('.episode-container').css('img').each do |image|
          image_url = Addressable::URI.join(obsession['href'], image['src']).to_s
          unless images.key? image_url
            p "Downloading #{index_obsession}-#{index_episode} #{image_url}"
            current_image_index = images.length
            http = Curl.get(image_url)
            image_extension = Addressable::URI.parse(image_url).extname
            image_file_name = "#{current_image_index}#{image_extension}"
            open(File.join(episode_dir, image_file_name), 'w') do |f|
              f.puts http.body_str
            end
            images[image_url] = image_file_name
            sleep(1)            
          end
        end
        
        File.open(episode_images_infos, 'w') do |f|
          f.write(JSON.pretty_generate(
            infos = {
              'logo': "logo#{logo_image_extension}",
              'images': images,
            }
    
          ))
        end
      end
    
    end
  end
end
