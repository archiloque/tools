#!/usr/bin/env ruby

require 'nokogiri'
require 'json'
require 'fileutils'
require 'addressable'

require_relative 'constants'

def create_if_not_exist(dir_path)
  unless File.exist? dir_path
    Dir.mkdir dir_path
  end
end

create_if_not_exist(BOOK_DIR)

IMAGES_TO_PROCESS = {}

def process_content(content, images, index_obsession, index_episode, episode_dir, episode_url)
  result = ''
  images_index = 0
  if images['logo']
    IMAGES_TO_PROCESS[File.join(episode_dir, images['logo'])] = File.join(BOOK_DIR, index_obsession.to_s, index_episode.to_s, images['logo'])
    result << "image::#{index_obsession}/#{index_episode}/#{images['logo']}[]\n\n"
  end

  content.at_css('.episode-container').children.each do |node|
    if node.text?
      stripped_text = node.text.gsub(/\u00A0/,' ').gsub(/\u200B/ , '').strip
      if ['', '.'].include? stripped_text
      else
        raise "Not empty text [#{stripped_text.codepoints.map{|c| '\u%04X' % c}.join}] [#{node}]"
      end
    elsif node.element?
      if node.name == 'i'
        result << "__#{node.text.strip}__\n\n"
      elsif ['p', 'i', 'div'].include? node.name
        node.css('i').each do |i|
          inner_text = i.inner_text.strip
          if inner_text == ''
            i.remove
          else
            new_node = content.create_element 'span'
            new_node.content = " __#{inner_text}__ "
            i.replace new_node
          end
        end
        result << "#{node.text.strip.gsub(/\s+/, ' ')}\n\n"
      elsif ['br', 'aside', 'blockquote', 'iframe', 'script'].include? node.name
      elsif ['ul'].include? node.name
          # TODO
          result << "#{node.text}\n"
      elsif node.name == 'figure'
        images_nodes = node.css('img')
          if images_nodes.length != 0
            image_url = images_nodes[0]['src']
            image_url = Addressable::URI.join(episode_url, image_url).to_s
            # p "Processing [#{image_url}]"

            image_file_name = images['images'][image_url]
            image_target_file = "#{index_obsession}/#{index_episode}/#{image_file_name}"

            IMAGES_TO_PROCESS[File.join(episode_dir, image_file_name)] = File.join(BOOK_DIR, index_obsession.to_s, index_episode.to_s, image_file_name)
            caption_node = node.at_css('figcaption')
            if caption_node
              caption = caption_node.inner_text.strip.gsub(/\n/,' ').gsub(/\s+/, ' ')
              result << "image::#{image_target_file}[title=\"#{caption}\"]\n\n"
            else
              result << "image::#{image_target_file}[]\n\n"
            end
            images_index = images_index + 1
          end
      elsif node.name == 'h3'
        result << "=== #{node.text}\n\n"  
      elsif node.name == 'hr'
        result << "'''\n\n"  
      else
        raise "Unknown name [#{node.name}]  #{node}"
      end
    elsif node.comment?
    else
      raise "[#{node}]"
    end
  end
  result
end

if File.exist? 'logo.png'
  FileUtils.copy_file 'logo.png', File.join(BOOK_DIR, 'logo.png')
end

chapters = []

time = Time.new
version_string = "v1.0, #{time.year}-#{time.month}-#{time.day}\n"

obsessions = JSON.parse(IO.read(OBSESSIONS_WITH_EPISODES_JSON_FILE))
obsessions.each_with_index do |obsession, index_obsession|
  p "Processing obsession #{index_obsession} [#{obsession['href']}]"
  obsession_target_dir = File.join(BOOK_DIR, index_obsession.to_s)
  create_if_not_exist(obsession_target_dir)
  
  obsession_articles = []
  obsession['articles'].each_with_index do |episode, index_episode|
    episode_dir = File.join(OBSESSIONS_DIR, index_obsession.to_s, index_episode.to_s)
    source_episode_file = File.join(episode_dir, 'index.html')
    image_episode_file = File.join(episode_dir, 'images.json')
    if File.exist?(source_episode_file) && File.exist?(image_episode_file)
      episode_target_dir = File.join(obsession_target_dir, index_episode.to_s)
      create_if_not_exist(episode_target_dir)

      chapter_name = "#{index_obsession}-#{index_episode}"
      target_episode_file = File.join(BOOK_DIR, index_obsession.to_s, index_episode.to_s, 'index.adoc')
      p "Processing episode #{index_obsession}-#{index_episode} [#{source_episode_file}] to [#{target_episode_file}]"
      doc = File.open(source_episode_file) { |f| Nokogiri::HTML(f) }
      images_infos = JSON.parse(IO.read(image_episode_file))
      open(target_episode_file, 'w') do |episode_content|
        episode_content << "[##{chapter_name}]\n"
        episode_content << "== #{doc.at_css('title').text}\n"
        episode_content << ":lang: fr\n"
        episode_content << ":figure-caption!:\n"
        episode_content << "\n"
        episode_content << "#{process_content(doc, images_infos, index_obsession, index_episode, episode_dir, episode['href'])}"
      end
      obsession_articles << File.join(index_obsession.to_s, index_episode.to_s, 'index.adoc')
    end
  end

  if obsession_articles.empty?
    p "Skipping it !"
  else
    open(File.join(BOOK_DIR, "#{index_obsession}.adoc"), 'w') do |obsession_content|
      obsession_content << "[##{index_obsession}]\n"
      obsession_content << "= #{Nokogiri::HTML::fragment(obsession['name']).inner_text.strip.gsub(/\s+/, ' ')}\n"
      obsession_content << "La rédaction des jours\n"
      obsession_content << ":lang: fr\n"
      obsession_content << ":figure-caption!:\n"
      obsession_content << "\n"
      obsession_articles.reverse.each do |article|
        obsession_content << "include::#{article}[]\n"
      end
    end
    chapters << index_obsession    

  end
  
end

p "Processing #{IMAGES_TO_PROCESS.length} images"
image_convert_files = File.join(BOOK_DIR, "convert_images.sh")
open(image_convert_files, 'w') do |conversion_file|
  IMAGES_TO_PROCESS.each_pair do |key, value|
    conversion_file << "#{IMAGE_TRANSFORMATION_SCRIPT} #{key} #{value}\n"
  end
end
`gm batch -stop-on-error on #{image_convert_files}`

open(File.join(BOOK_DIR, 'index.adoc'), 'w') do |main_book_content|
  main_book_content << "= Les jours\n"
  main_book_content << "La rédaction des jours\n"
  main_book_content << version_string
  main_book_content << ":doctype: book\n"
  main_book_content << ":lang: fr\n"
  main_book_content << ":figure-caption!:\n"
  if File.exist? 'logo.png'
    main_book_content << ":front-cover-image: image:logo.png[width=400,height=400]\n"
  end
  main_book_content << "\n"
  chapters.reverse.each do |chapter|
    main_book_content << "include::#{chapter}.adoc[]\n"
  end
end

p "Converting"
`asciidoctor-epub3 -D output data/book/index.adoc`
`asciidoctor-epub3 -D output -a ebook-format=kf8 data/book/index.adoc`
