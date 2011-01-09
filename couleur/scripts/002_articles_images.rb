require 'rubygems'
require 'lib/models'
require 'lib/magic'

Article.filter('image_url is not null and colors is null').each do |article|
  colors = identify_main_colors(article.image_path, 3)
  article.colors = colors.join(",")
  article.save
end

