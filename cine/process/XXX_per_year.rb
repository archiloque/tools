require 'rubygems'
require 'process/XXX_common'

# do the calculations

# params : year and number of films
QUERY_PEOPLE_GENRES = "select count(*) c, sum(average_people) s, (sum(average_people) / count(*)) a, g.name n, g.id i " +
        "from films f, genres g, films_genres fg " +
        "where f.id = fg.film_id and fg.genre_id = g.id and f.average_people is not null and strftime('%Y', pub_date) = ? " +
        "group by g.id " +
        "having c >= ? " +
        "order by a desc limit 10"

# param: year and number of films
QUERY_PRESS_GENRES = "select count(*) c, sum(average_press) s, (sum(average_press) / count(*)) a, g.name n, g.id i " +
        "from films f, genres g, films_genres fg " +
        "where f.id = fg.film_id and fg.genre_id = g.id and f.average_press is not null and strftime('%Y', pub_date) = ? " +
        "group by g.id "+
        "having c >= ? " +
        "order by a desc limit 10"

# param: year, critic id and number of films
QUERY_CRITIC_GENRES = "select count(*) c, sum(value) s, (CAST(sum(value) AS FLOAT)/ count(*)) a, ge.name n, ge.id i " +
        "from genres ge, films_genres fg, grades gr, films f " +
        "where strftime('%Y', pub_date) = ? and gr.film_id = fg.film_id and fg.genre_id = ge.id and gr.critic_id = ? and f.id = fg.film_id " +
        "group by ge.id " +
        "having c >= ? " +
        "order by a desc limit 10"

QUERY_PEOPLE_TAGS = "select count(*) c, sum(average_people) s, (sum(average_people) / count(*)) a, g.name n, g.id i " +
        "from films f, tags g, films_tags fg " +
        "where f.id = fg.film_id and fg.tag_id = g.id and f.average_people is not null and strftime('%Y', pub_date) = ? " +
        "group by g.id " +
        "having c >= ? " +
        "order by a desc limit 10"

QUERY_PRESS_TAGS = "select count(*) c, sum(average_press) s, (sum(average_press) / count(*)) a, g.name n, g.id i " +
        "from films f, tags g, films_tags fg " +
        "where f.id = fg.film_id and fg.tag_id = g.id and f.average_press is not null and strftime('%Y', pub_date) = ? " +
        "group by g.id "+
        "having c >= ? " +
        "order by a desc limit 10"

QUERY_CRITIC_TAGS = "select count(*) c, sum(value) s, (CAST(sum(value) AS FLOAT)/ count(*)) a, ge.name n, ge.id i " +
        "from tags ge, films_tags fg, grades gr, films f " +
        "where strftime('%Y', pub_date) = ? and gr.film_id = fg.film_id and fg.tag_id = ge.id and gr.critic_id = ? and f.id = fg.film_id " +
        "group by ge.id " +
        "having c >= ? " +
        "order by a desc limit 10"

QUERY_PEOPLE_TAGS_CANNES = "select count(*) c, sum(average_people) s, (sum(average_people) / count(*)) a, g.name n, g.id i " +
        "from films f, tags g, films_tags fg " +
        "where f.id = fg.film_id and fg.tag_id = g.id and f.average_people is not null and strftime('%Y', pub_date) = ? and g.name not like '%Cannes%' " +
        "group by g.id " +
        "having c >= ? " +
        "order by a desc limit 10"

QUERY_PRESS_TAGS_CANNES = "select count(*) c, sum(average_press) s, (sum(average_press) / count(*)) a, g.name n, g.id i " +
        "from films f, tags g, films_tags fg " +
        "where f.id = fg.film_id and fg.tag_id = g.id and f.average_press is not null and strftime('%Y', pub_date) = ? and g.name not like '%Cannes%' " +
        "group by g.id "+
        "having c >= ? " +
        "order by a desc limit 10"

QUERY_CRITIC_TAGS_CANNES = "select count(*) c, sum(value) s, (CAST(sum(value) AS FLOAT)/ count(*)) a, ge.name n, ge.id i " +
        "from tags ge, films_tags fg, grades gr, films f " +
        "where strftime('%Y', pub_date) = ? and gr.film_id = fg.film_id and fg.tag_id = ge.id and gr.critic_id = ? and f.id = fg.film_id and ge.name not like '%Cannes%' " +
        "group by ge.id " +
        "having c >= ? " +
        "order by a desc limit 10"


# each is a map whose keys are the year
people_genre_5 = Hash.new { |hash, key| hash[key] = [] }
people_genre_10 = Hash.new { |hash, key| hash[key] = [] }
press_genre_5 = Hash.new { |hash, key| hash[key] = [] }
press_genre_10 = Hash.new { |hash, key| hash[key] = [] }
critics_genre_5 = Hash.new { |hash, key| hash[key] = {} }
critics_genre_10 = Hash.new { |hash, key| hash[key] = {} }

people_tag_5 = Hash.new { |hash, key| hash[key] = [] }
people_tag_10 = Hash.new { |hash, key| hash[key] = [] }
press_tag_5 = Hash.new { |hash, key| hash[key] = [] }
press_tag_10 = Hash.new { |hash, key| hash[key] = [] }
critics_tag_5 = Hash.new { |hash, key| hash[key] = {} }
critics_tag_10 = Hash.new { |hash, key| hash[key] = {} }

people_tag_cannes_5 = Hash.new { |hash, key| hash[key] = [] }
people_tag_cannes_10 = Hash.new { |hash, key| hash[key] = [] }
press_tag_cannes_5 = Hash.new { |hash, key| hash[key] = [] }
press_tag_cannes_10 = Hash.new { |hash, key| hash[key] = [] }
critics_tag_cannes_5 = Hash.new { |hash, key| hash[key] = {} }
critics_tag_cannes_10 = Hash.new { |hash, key| hash[key] = {} }

2000.upto(2010) do |year|
  DB.fetch(QUERY_PEOPLE_GENRES, year.to_s, 5) do |row|
    people_genre_5[year] << row
  end
  DB.fetch(QUERY_PEOPLE_GENRES, year.to_s, 10) do |row|
    people_genre_10[year] << row
  end
  DB.fetch(QUERY_PRESS_GENRES, year.to_s, 5) do |row|
    press_genre_5[year] << row
  end
  DB.fetch(QUERY_PRESS_GENRES, year.to_s, 10) do |row|
    press_genre_10[year] << row
  end

  DB.fetch(QUERY_PEOPLE_TAGS, year.to_s, 5) do |row|
    people_tag_5[year] << row
  end
  DB.fetch(QUERY_PEOPLE_TAGS, year.to_s, 10) do |row|
    people_tag_10[year] << row
  end
  DB.fetch(QUERY_PRESS_TAGS, year.to_s, 5) do |row|
    press_tag_5[year] << row
  end
  DB.fetch(QUERY_PRESS_TAGS, year.to_s, 10) do |row|
    press_tag_10[year] << row
  end

  DB.fetch(QUERY_PEOPLE_TAGS_CANNES, year.to_s, 5) do |row|
    people_tag_cannes_5[year] << row
  end
  DB.fetch(QUERY_PEOPLE_TAGS_CANNES, year.to_s, 10) do |row|
    people_tag_cannes_10[year] << row
  end
  DB.fetch(QUERY_PRESS_TAGS_CANNES, year.to_s, 5) do |row|
    press_tag_cannes_5[year] << row
  end
  DB.fetch(QUERY_PRESS_TAGS_CANNES, year.to_s, 10) do |row|
    press_tag_cannes_10[year] << row
  end

  Critic.order(:name).each do |critic|

    data = []
    DB.fetch(QUERY_CRITIC_GENRES, year.to_s, critic.id, 5) do |row|
      data << row
    end
    unless data.empty?
      critics_genre_5[year][critic.name] = data
    end

    data = []
    DB.fetch(QUERY_CRITIC_GENRES, year.to_s, critic.id, 10) do |row|
      data << row
    end
    unless data.empty?
      critics_genre_10[year][critic.name] = data
    end

    data = []
    DB.fetch(QUERY_CRITIC_TAGS, year.to_s, critic.id, 5) do |row|
      data << row
    end
    unless data.empty?
      critics_tag_5[year][critic.name] = data
    end

    data = []
    DB.fetch(QUERY_CRITIC_TAGS, year.to_s, critic.id, 10) do |row|
      data << row
    end
    unless data.empty?
      critics_tag_10[year][critic.name] = data
    end

    data = []
    DB.fetch(QUERY_CRITIC_TAGS_CANNES, year.to_s, critic.id, 5) do |row|
      data << row
    end
    unless data.empty?
      critics_tag_cannes_5[year][critic.name] = data
    end

    data = []
    DB.fetch(QUERY_CRITIC_TAGS_CANNES, year.to_s, critic.id, 10) do |row|
      data << row
    end
    unless data.empty?
      critics_tag_cannes_10[year][critic.name] = data
    end

  end
end

def record_people row
  "<span class='#{PEOPLE_CLASS}'>#{row[:n]} (#{format("%.2f", row[:a])})</span>"
end

def record_press row, people
  if people.any? { |r| r[:i] == row[:i] }
    "<span class='#{PEOPLE_CLASS} #{PRESS_CLASS}'>#{row[:n]} (#{format("%.2f", row[:a])})</span>"
  else
    "<span class='#{PRESS_CLASS}'>#{row[:n]} (#{format("%.2f", row[:a])})</span>"
  end
end

def record_critic row, people, press
  classes = []
  if people.any? { |r| r[:i] == row[:i] }
    classes << 'people'
  end
  if press.any? { |r| r[:i] == row[:i] }
    classes << 'press'
  end

  classes_text = classes.empty? ? '' : " class='#{classes.join(' ')}'"
  "<span#{classes_text}>#{row[:n]} (#{format("%.2f", row[:a])})</span>"
end

def public_to_column list
  result = [PUBLIC_TITLE]
  list.each do |row|
    result << record_people(row)
  end
  result
end

def press_to_column list, people
  result = [PRESS_TITLE]
  list.each do |row|
    result << record_press(row, people)
  end
  result
end

def critic_to_column list, press, people
  results = []
  list.keys.sort.each do |name|
    data = [name]
    list[name].each do |row|
      data << record_critic(row, people, press)
    end
    unless data.length == 1
      results << data
    end
  end
  results
end

begin
  def per_year out, people, press, critics, year
    results = []
    results << public_to_column(people[year])
    results << press_to_column(press[year], people[year])
    results.concat(critic_to_column(critics[year], press[year], people[year]))
    to_table results, out
  end

# for each year
  2000.upto(2010) do |year|
    result = IO.read("process/erb/head.erb.html").gsub('@@Title@@', year.to_s)

    result << '<h1>Genres, au moins <b>10</b> films</h1>'
    per_year result, people_genre_10, press_genre_10, critics_genre_10, year

    result << '<h1>Genres, au moins <b>5</b> films</h1>'
    per_year result, people_genre_5, press_genre_5, critics_genre_5, year

    result << '<h1>Tags, au moins <b>10</b> films</h1>'
    per_year result, people_tag_10, press_tag_10, critics_tag_10, year

    result << '<h1>Tags, au moins <b>5</b> films</h1>'
    per_year result, people_tag_5, press_tag_5, critics_tag_5, year

    result << '<h1>Tags hors Cannes, au moins <b>10</b> films</h1>'
    per_year result, people_tag_cannes_10, press_tag_cannes_10, critics_tag_cannes_10, year

    result << '<h1>Tags hors Cannes, au moins <b>5</b> films</h1>'
    per_year result, people_tag_cannes_5, press_tag_cannes_5, critics_tag_cannes_5, year

    result << IO.read("process/erb/tail.erb.html")
    File.open("out/annee/#{year}.html", 'w') { |f| f.write(result) }
  end
end

begin
  def per_critic out, critic, people_data, press_data, critic_data
    results = []
    2000.upto(2010) do |year|
      if data = critic_data[year][critic.name]
        r = [year.to_i]
        data.each do |d|
          r << record_critic(d, press_data[year], people_data[year])
        end
        results << r
      end
    end
    to_table results, out
  end

# for each critic
  Critic.each do |critic|

    result = IO.read("process/erb/head.erb.html").gsub('@@Title@@', critic.name)

    result << '<h1>Genres, au moins <b>10</b> films</h1>'
    per_critic result, critic, people_genre_10, press_genre_10, critics_genre_10

    result << '<h1>Genres, au moins <b>5</b> films</h1>'
    per_critic result, critic, people_genre_5, press_genre_5, critics_genre_5

    result << '<h1>Tags, au moins <b>10</b> films</h1>'
    per_critic result, critic, people_tag_10, press_tag_10, critics_tag_10

    result << '<h1>Tags, au moins <b>5</b> films</h1>'
    per_critic result, critic, people_tag_5, press_tag_5, critics_tag_5

    result << '<h1>Tags hors Cannes, au moins <b>10</b> films</h1>'
    per_critic result, critic, people_tag_cannes_10, press_tag_cannes_10, critics_tag_cannes_10

    result << '<h1>Tags hors Cannes, au moins <b>5</b> films</h1>'
    per_critic result, critic, people_tag_cannes_5, press_tag_cannes_5, critics_tag_cannes_5

    result << IO.read("process/erb/tail.erb.html")
    File.open("out/presse/#{critic.name}.html", 'w') { |f| f.write(result) }
  end
end

begin
  result = IO.read("process/erb/head.erb.html").gsub('@@Title@@', "Public")

  def people_data out, data
    results = []
    2000.upto(2010) do |year|
      r = [year.to_i]
      data[year].each do |d|
        r << record_people(d)
      end
      results << r
    end
    to_table results, out
  end

  result << '<h1>Genres, au moins <b>10</b> films</h1>'
  people_data result, people_genre_10

  result << '<h1>Genres, au moins <b>5</b> films</h1>'
  people_data result, people_genre_5

  result << '<h1>Tags, au moins <b>10</b> films</h1>'
  people_data result, people_tag_10

  result << '<h1>Tags, au moins <b>5</b> films</h1>'
  people_data result, people_tag_5

  result << '<h1>Tags hors Cannes, au moins <b>10</b> films</h1>'
  people_data result, people_tag_cannes_10

  result << '<h1>Tags hors Cannes, au moins <b>5</b> films</h1>'
  people_data result, people_tag_cannes_5

  result << IO.read("process/erb/tail.erb.html")
  File.open("out/public.html", 'w') { |f| f.write(result) }
end


begin
  result = IO.read("process/erb/head.erb.html").gsub('@@Title@@', "Presse")

  def press_data out, public, press
    results = []
    2000.upto(2010) do |year|
      r = [year.to_i]
      press[year].each do |d|
        r << record_press(d, public[year])
      end
      results << r
    end
    to_table results, out
  end

  result << '<h1>Genres, au moins <b>10</b> films</h1>'
  press_data result, people_genre_10, press_genre_10

  result << '<h1>Genres, au moins <b>5</b> films</h1>'
  press_data result, people_genre_5, press_genre_5

  result << '<h1>Tags, au moins <b>10</b> films</h1>'
  press_data result, people_tag_10, press_tag_10

  result << '<h1>Tags, au moins <b>5</b> films</h1>'
  press_data result, people_tag_5, press_tag_5

  result << '<h1>Tags hors Cannes, au moins <b>10</b> films</h1>'
  press_data result, people_tag_cannes_10, press_tag_cannes_10

  result << '<h1>Tags hors Cannes, au moins <b>5</b> films</h1>'
  press_data result, people_tag_cannes_5, press_tag_cannes_5

  result << IO.read("process/erb/tail.erb.html")
  File.open("out/presse.html", 'w') { |f| f.write(result) }
end