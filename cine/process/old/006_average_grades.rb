require 'rubygems'
require 'database'

DB.transaction do
  Genre.each do |genre|
    # all grades that concern this genre
    DB.fetch("select count(*) co, sum(films.average_press) apr, sum(films.average_people) ape from films, films_genres where films.id = films_genres.film_id and films_genres.genre_id = ?", genre.id) do |row|
    	GradesGenre.create(:genre_id => genre.id, :number => row[:co], :average_press => (row[:apr].to_f / row[:co].to_f), :average_people => (row[:ape].to_f / row[:co].to_f))
    end
  end
    
  Tag.each do |tag|
    # all grades that concern this tag
    DB.fetch("select count(*) co, sum(films.average_press) apr, sum(films.average_people) ape from films, films_tags where films.id = films_tags.film_id and films_tags.tag_id = ?", tag.id) do |row|
    	GradesTag.create(:tag_id => tag.id, :number => row[:co], :average_press => (row[:apr].to_f / row[:co].to_f), :average_people => (row[:ape].to_f / row[:co].to_f))
    end
  end

end

