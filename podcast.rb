# Generate a podcast rss file from all the mp3 contained in a directory
# The mp3 are named "YYYY-MM-DD??????" and we get their title from their id3 tags
# Params
# - directory of mp3 files, default '.'
# - root url for the mp3s, default 'http:/localhost/'
# - the podcast title, default 'podcast'
# - image url
# - a target directory where the mp3 files will be copied with a short name if param is here, usefull if you have strange names that aren't url-firendly
# Julien Kirch 2009, available under MIT license

require 'rubygems'
require 'id3'
require 'date'
require 'parsedate'
require 'builder'
require 'rss/2.0'

# An item in the feed
class RssItem
  include Comparable
  attr_accessor :title, :file_name, :duration, :pub_date, :size
  
  def initialize
    title = ''
  end
  
  def to_s
    "\"#{file_name}\", \"#{title}\", #{duration}, #{pub_date}"
  end
  
  def <=> o
    pub_date <=> o.pub_date
  end
end

mp3_directory = ARGV[0] || '.'
root_url = ARGV[1] || 'http://localhost/'
title = ARGV[2] || 'podcast'
image_url = ARGV[3]
target_directory = ARGV[4]

mp3s = Array.new
Dir.chdir(mp3_directory)
Dir.glob('*.mp3') do |file_name|
  item = RssItem.new
  file = ID3::AudioFile.new file_name
  if file.tagID3v1
    item.title = file.tagID3v1['TITLE']
  end
  if item.title.nil? || item.title.empty?
    item.title = File.basename(file_name, '.*')
  end
  item.duration = file.audioLength
  date = ParseDate.parsedate file_name[0,10]
  item.pub_date = Time.gm(date[0], date[1], date[2], 0, 0, 0)
  item.size = File.size(file_name)
  if target_directory
    target_name = sprintf("%03d", mp3s.size + 1) + File.extname(file_name)
    File.copy(file_name, target_directory + target_name)
    item.file_name = target_name
  else
    item.file_name = file_name
  end
  mp3s << item
end

mp3s.sort

result = ''
xml = Builder::XmlMarkup.new(:target=> result, :indent => 2)
xml.instruct! 'xml', :version => '1.0', :encoding => 'UTF-8'
xml.rss(:version=>"2.0", 'xmlns:itunes' =>'http://www.itunes.com/dtds/podcast-1.0.dtd') {
  xml.channel {
    xml.title title
    xml.description title
    if image_url && (!image_url.empty?)
      xml.image {
        xml.url image_url
        xml.title title
        xml.link root_url
      }
    end
    mp3s.each do |mp3|
      xml.item {
        xml.title mp3.title
        xml.link(root_url + mp3.file_name)
        xml.enclosure(:url => (root_url + mp3.file_name), :length => mp3.size, :type => 'audio/mpeg')
        xml.guid(root_url + mp3.file_name)
        xml.pubDate mp3.pub_date.rfc2822
      }
    end
  }
}
print result

