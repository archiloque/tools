require 'rubygems'
require 'database'
require 'nokogiri'
require 'open-uri'

# personne/fichepersonne_gen_cpersonne=81809.html
def get_id1 href
  href[(href.index('=') + 1)..(href.rindex('.') - 1)].to_i
end

# /film/tous/genre-13023/
def get_id2 href
  href[(href.index('-') + 1)..(href.index('/') - 1)].to_i
end

def percent_people film, doc, star_number
  text = doc.xpath("//a[@href = '/film/critiquepublic_gen_cfilm=#{film.custom_id}.html?stars=#{star_number}']").text
  if text.index(' ')
    text[0..(text.index(' ') - 1)].to_i
  else
    nil
  end
end

genres = {}
countries = {}
critics = {}
tags = {}

DB.transaction do
  Film.where(:scrapped => false).limit(500).each do |film|
    film.scrapped = true
    page_url = "http://www.allocine.fr/film/fichefilm_gen_cfilm=#{film.custom_id}.html"
    doc = Nokogiri::HTML(open(page_url), page_url, 'UTF-8')

    film.remove_all_directors
    doc.xpath("//comment()").detect { |o| o.text.include? 'End first block' }.parent.search('p')[1].children[1].search('a').each do |director_node|
      director_name = director_node.text
      director_id = get_id1(director_node[:href])
      director = Director.first(:custom_id => director_id) || Director.create(:name => director_name, :custom_id => director_id)
      film.add_director(director)
    end

    film.remove_all_countries
    doc.xpath("//a[starts-with(@href, '/film/tous/pays-')]").each do |country_node|
      country_id = get_id2(country_node[:href])

      country = countries[country_id]
      unless country
        country_name = country_node.text
        country = Country.first(:custom_id => country_id) || Country.create(:name => country_name, :custom_id => country_id)
        countries[country_id] = country
      end

      film.add_country(country)
    end


    film.remove_all_genres
    doc.xpath("//a[starts-with(@href, '/film/tous/genre-')]").each do |genre_node|
      genre_id = get_id2(genre_node[:href])

      genre = genres[genre_id]
      unless genre
        genre_name = genre_node.text
        genre = Genre.first(:custom_id => genre_id) || Genre.create(:name => genre_name, :custom_id => genre_id)
        genres[genre_id] = genre
      end

      film.add_genre(genre)
    end

    possible_press = doc.xpath("//a[starts-with(@href, '/film/revuedepresse_gen_cfilm=')]")
    unless possible_press.length == 0
      average_press_text = possible_press[0].parent.parent.text
      film.average_press = average_press_text[(average_press_text.index('(') + 1)..(average_press_text.index(')') - 1)].gsub(',', '.').to_f
    end

    possible_spectator = doc.xpath("//a[starts-with(@href, '/film/critiquepublic_gen_cfilm=')]")
    unless possible_spectator.length == 0
      average_people_text = possible_spectator[0].parent.parent.text
      if average_people_text.index('(')
        film.average_people = average_people_text[(average_people_text.rindex('(') + 1)..(average_people_text.rindex(')') - 1)].gsub(',', '.').to_f
      end
    end

    film.percent_people_1 = percent_people(film, doc, 1)
    film.percent_people_2 = percent_people(film, doc, 2)
    film.percent_people_3 = percent_people(film, doc, 3)
    film.percent_people_4 = percent_people(film, doc, 4)
    film.percent_people_5 = percent_people(film, doc, 5)

    Grade.where(:film_id => film.id).delete
    doc.xpath("//a[starts-with(@href, '/film/revuedepresse_gen_cfilm=#{film.custom_id}.html#pressreview')]").each do |critic_node|
      critic_name = critic_node.text
      grade = critic_node.parent.parent.children[2].search('img')[0][:title].to_i
      critic = critics[critic_name]
      unless critic
        critic = Critic.first(:name => critic_name) || Critic.create(:name => critic_name)
        critics[critic_name] = critic
      end

      Grade.create(:critic => critic, :film => film, :value => grade)
    end

    film.remove_all_tags
    doc.xpath("//a[starts-with(@href, '/tags/tag-')]").each do |tag_node|
      tag_id = get_id2(tag_node[:href])
      tag = tags[tag_id]
      unless tag
        tag_content = tag_node.text.strip
        tag_name = tag_content[0..(tag_content.rindex('(') - 3)]
        tag = Tag.first(:custom_id => tag_id) || Tag.create(:name => tag_name, :custom_id => tag_id)
        tags[tag_id] = tag
      end
      film.add_tag tag
    end

    film.save
  end
end