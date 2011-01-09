require 'rubygems'
require 'lib/models'
require 'lib/magic'
require 'tempfile'
require 'erb'

DB["CREATE AGGREGATE textcat_all(basetype    = text, sfunc       = textcat, stype       = text, initcond    = '')"]

all_data = []
result   = []
DB["select tags.text, tags.id, count(articles_tags.article_id) as c from tags, articles_tags, articles where tags.id = articles_tags.tag_id and articles_tags.article_id = articles.id and articles.colors is not null group by tags.id, tags.text order by c desc limit 500"].each do |r1|
  tag_name = r1[:text]
  tag_id   = r1[:id]
  DB["SELECT textcat_all(articles.colors) t FROM articles, articles_tags where articles.colors is not null and articles.id = articles_tags.article_id and articles_tags.tag_id = ?", tag_id].each do |r2|
    pixels   = r2[:t].split(",")
    all_data += pixels
    File.open('image.txt', 'w') { |f| f.write(pixels_to_image(pixels)) }
    result << {:name => tag_name, :colors => identify_main_colors('image.txt', 8)}
  end
end
File.open('image.txt', 'w') { |f| f.write(pixels_to_image(all_data)) }
all            = identify_main_colors('image.txt', 20)
# all = ['4C3C36', '5B5655', 'B64E34', '90776F', '6D8E52', '9E8D69', 'CEA257', '3156B9', '687092', '8E748C', '6990AC', 'A39A94', 'B3B1B0', 'CBB5A7', 'D4C8B4', 'ADB7CA', 'BAC5CE', 'CFCECC', 'E7E5E3']
all_colors     = all.collect { |c| color_to_number(c) }

result.each do |r|
  r[:colors].delete_if do |c1|
    c2 = color_to_number(c1)
    all_colors.any? { |c3| distance(c3, c2) < 300 }
  end
end

STDOUT << ERB.new(File.new('scripts/003.erb').read).result(binding)
