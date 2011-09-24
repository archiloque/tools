require 'sequel'

DB = Sequel.connect('postgres://couleur:couleur@localhost/duplication')

class Article < Sequel::Model
 many_to_many :tags
 def image_path
   "images/#{id}#{File.extname(image_url)}"
 end
end

class Tag < Sequel::Model
 many_to_many :articles
end