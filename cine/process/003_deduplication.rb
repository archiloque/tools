require 'rubygems'
require 'database'

# some tags and directors are duplicated in the scrapped data, we will deduplicate the data here

DB.fetch("select count(*) c, min(custom_id) i, name n from tags group by name having c > 1") do |row|
  p "Tag #{row[:n]}"
  DB["update films_tags set tag_id = (select id from tags where custom_id = ?) where tag_id in (select id from tags where name = ? and custom_id != ?", row[:i], row[:n], row[:i]]
  DB["delete from tags where name = ? and custom_id != ?", row[:n], row[:i]]
end

DB.fetch("select count(*) c, min(custom_id) i, name n from directors group by name having c > 1") do |row|
  p "Director #{row[:n]}"
  DB["update directors_films set director_id = (select id from directors where custom_id = ?) where director_id in (select id from directors where name = ? and custom_id != ?", row[:i], row[:n], row[:i]]
  DB["delete from directors where name = ? and custom_id != ?", row[:n], row[:i]]
end

DB.fetch("select count(*) c, min(custom_id) i, name n from countries group by name having c > 1") do |row|
  p "Country #{row[:n]}"
  DB["update countries_films set country_id = (select id from countries where custom_id = ?) where country_id in (select id from countries where name = ? and custom_id != ?", row[:i], row[:n], row[:i]]
  DB["delete from countries where name = ? and custom_id != ?", row[:n], row[:i]]
end
