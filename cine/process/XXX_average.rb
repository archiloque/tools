require 'rubygems'
require 'process/XXX_common'


def record_people row
  "<span class='#{PEOPLE_CLASS}'>#{row[:n]} (#{format("%.2f", row[:a])})</span>"
end

def record_press row, people
  if people.include? row[:i]
    "<span class='#{PEOPLE_CLASS} #{PRESS_CLASS}'>#{row[:n]} (#{format("%.2f", row[:a])})</span>"
  else
    "<span class='#{PRESS_CLASS}'>#{row[:n]} (#{format("%.2f", row[:a])})</span>"
  end
end

def record_critic row, people, press
  classes = []
  if people.include? row[:i]
    classes << 'people'
  end
  if press.include? row[:i]
    classes << 'press'
  end

  classes_text = classes.empty? ? '' : " class='#{classes.join(' ')}'"
  "<span#{classes_text}>#{row[:n]} (#{format("%.2f", row[:a])})</span>"
end


puts IO.read("process/erb/head.erb.html").gsub('@@Title@@', 'Moyenne')

# genres

# param: number of films
QUERY_PEOPLE_GENRES = "select count(*) c, sum(average_people) s, (sum(average_people) / count(*)) a, g.name n, g.id i " +
        "from films f, genres g, films_genres fg " +
        "where f.id = fg.film_id and fg.genre_id = g.id and f.average_people is not null " +
        "group by g.id " +
        "having c >= ? " +
        "order by a desc limit 10"

# param: number of films
QUERY_PRESS_GENRES = "select count(*) c, sum(average_press) s, (sum(average_press) / count(*)) a, g.name n, g.id i " +
        "from films f, genres g, films_genres fg " +
        "where f.id = fg.film_id and fg.genre_id = g.id and f.average_press is not null " +
        "group by g.id "+
        "having c >= ? " +
        "order by a desc limit 10"

# param: critic id and number of films
QUERY_CRITIC_GENRES = "select count(*) c, sum(value) s, (CAST(sum(value) AS FLOAT)/ count(*)) a, ge.name n, ge.id i " +
        "from genres ge, films_genres fg, grades gr " +
        "where gr.film_id = fg.film_id and fg.genre_id = ge.id and gr.critic_id = ? " +
        "group by ge.id " +
        "having c >= ? " +
        "order by a desc limit 10"

begin

  puts '<h1>Genres, au moins <b>10</b> films</h1>'

  results = []

  results << [PUBLIC_TITLE]
  people_ids = []
  DB.fetch(QUERY_PEOPLE_GENRES, 10) do |row|
    people_ids << row[:i]
    results[0] << record_people(row)
  end

  results << [PRESS_TITLE]

  press_ids = []
  DB.fetch(QUERY_PRESS_GENRES, 10) do |row|
    press_ids << row[:i]
    results[1] << record_press(row, people_ids)
  end

  Critic.order(:name).each do |critic|
    data = [critic.name]
    DB.fetch(QUERY_CRITIC_GENRES, critic.id, 10) do |row|
      data << record_critic(row, people_ids, press_ids)
    end
    unless data.length == 1
      results << data
    end
  end

  to_table results, STDOUT
end
begin
  puts '<h1>Genres, au moins <b>5</b> films</h1>'

  results = []

  results << [PUBLIC_TITLE]
  people_ids = []
  DB.fetch(QUERY_PEOPLE_GENRES, 5) do |row|
    people_ids << row[:i]
    results[0] << record_people(row)
  end

  results << [PRESS_TITLE]

  press_ids = []
  DB.fetch(QUERY_PRESS_GENRES, 5) do |row|
    press_ids << row[:i]
    results[1] << record_press(row, people_ids)
  end

  Critic.order(:name).each do |critic|
    data = [critic.name]
    DB.fetch(QUERY_CRITIC_GENRES, critic.id, 5) do |row|
      data << record_critic(row, people_ids, press_ids)
    end
    unless data.length == 1
      results << data
    end
  end

  to_table results, STDOUT

end

QUERY_PEOPLE_TAGS = "select count(*) c, sum(average_people) s, (sum(average_people) / count(*)) a, g.name n, g.id i " +
        "from films f, tags g, films_tags fg " +
        "where f.id = fg.film_id and fg.tag_id = g.id and f.average_people is not null " +
        "group by g.id " +
        "having c >= ? " +
        "order by a desc limit 10"

QUERY_PRESS_TAGS = "select count(*) c, sum(average_press) s, (sum(average_press) / count(*)) a, g.name n, g.id i " +
        "from films f, tags g, films_tags fg " +
        "where f.id = fg.film_id and fg.tag_id = g.id and f.average_press is not null " +
        "group by g.id "+
        "having c >= ? " +
        "order by a desc limit 10"

QUERY_CRITIC_TAGS = "select count(*) c, sum(value) s, (CAST(sum(value) AS FLOAT)/ count(*)) a, ge.name n, ge.id i " +
        "from tags ge, films_tags fg, grades gr " +
        "where gr.film_id = fg.film_id and fg.tag_id = ge.id and gr.critic_id = ? " +
        "group by ge.id " +
        "having c >= ? " +
        "order by a desc limit 10"

# tags
begin
  puts '<h1>Tags, au moins <b>10</b> films</h1>'

  results = []

  results << [PUBLIC_TITLE]
  people_ids = []
  DB.fetch(QUERY_PEOPLE_TAGS, 10) do |row|
    people_ids << row[:i]
    results[0] << record_people(row)
  end

  results << [PRESS_TITLE]

  press_ids = []
  DB.fetch(QUERY_PRESS_TAGS, 10) do |row|
    press_ids << row[:i]
    results[1] << record_press(row, people_ids)
  end

  Critic.order(:name).each do |critic|
    data = [critic.name]
    DB.fetch(QUERY_CRITIC_TAGS, critic.id, 10) do |row|
      data << record_critic(row, people_ids, press_ids)
    end
    unless data.length == 1
      results << data
    end
  end

  to_table results, STDOUT
end

# tags
begin
  puts '<h1>Tags, au moins <b>5</b> films</h1>'

  results = []

  results << [PUBLIC_TITLE]
  people_ids = []
  DB.fetch(QUERY_PEOPLE_TAGS, 5) do |row|
    people_ids << row[:i]
    results[0] << record_people(row)
  end

  results << [PRESS_TITLE]

  press_ids = []
  DB.fetch(QUERY_PRESS_TAGS, 5) do |row|
    press_ids << row[:i]
    results[1] << record_press(row, people_ids)
  end

  Critic.order(:name).each do |critic|
    data = [critic.name]
    DB.fetch(QUERY_CRITIC_TAGS, critic.id, 10) do |row|
      data << record_critic(row, people_ids, press_ids)
    end
    unless data.length == 1
      results << data
    end
  end

  to_table results, STDOUT

end

puts IO.read("process/erb/tail.erb.html")
