require 'rubygems'
require 'database'
require 'erb'

PUBLIC_TITLE = "<span class='people'>Public</span>"
PRESS_TITLE = "<span class='press'>Presse</span>"

def record_public row
	"<span class='people'>#{row[:n]} (#{format("%.2f", row[:a])})</span>"
end

def record_press row, people
	if people.include? row[:i]
		"<span class='people press'>#{row[:n]} (#{format("%.2f", row[:a])})</span>"
	else
		"<span class='press'>#{row[:n]} (#{format("%.2f", row[:a])})</span>"
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

genres = []

genres << [PUBLIC_TITLE]
people = []
DB.fetch('select g.id i, g.name n, gg.average_people a from grades_genres gg, genres g where gg.genre_id = g.id and gg.number > 5 order by average_people desc limit 5').each do |row|
	genres << [record_public(row)]
	people << row[:i]
end

genres[0] << [PRESS_TITLE]
press = []
i = 1
DB.fetch('select g.id i, g.name n, gg.average_press a from grades_genres gg, genres g where gg.genre_id = g.id and gg.number > 5 order by average_press desc limit 5').each do |row|
	press << row[:i]
	genres[i] << record_press(row, people)
	i += 1
end

DB.fetch('select name n, id i from critics order by critics.name').each do |r1|
	i = 1
	DB.fetch('select g.id i, g.name n, gc.average a from genres_per_critics gc, genres g where gc.critic_id = ? and g.id = gc.genre_id and gc.number > 5 order by gc.average desc limit 5', r1[:i]) do |r2|
		if i == 1
			genres[0] << r1[:n]
		end
		
		genres[i] << record_critic(r2, people, press)
		i += 1
	end
	if i != 1
		while i <= 5
			genres[i] << ''
			i += 1
		end
	end
end






tags = []
people = []
tags << [PUBLIC_TITLE]
DB.fetch('select g.id i, g.name n, gg.average_people a from grades_tags gg, tags g where gg.tag_id = g.id and gg.number > 5 order by average_people desc limit 5').each do |row|
	tags << [record_public(row)]
	people << row[:i]
end

tags[0] << [PRESS_TITLE]
press = []
i = 1
DB.fetch('select g.id i, g.name n, gg.average_press a from grades_tags gg, tags g where gg.tag_id = g.id and gg.number > 5 order by average_press desc limit 5').each do |row|
	press << row[:i]
	tags[i] << record_press(row, people)
	i += 1
end

DB.fetch('select name n, id i from critics order by critics.name').each do |r1|
	i = 1
	DB.fetch('select g.id i, g.name n, gc.average a from tags_per_critics gc, tags g where gc.critic_id = ? and g.id = gc.tag_id  and gc.number > 5 order by average desc limit 5', r1[:i]) do |r2|
		if i == 1
			tags[0] << r1[:n]
		end
		tags[i] << record_critic(r2, people, press)
		i += 1
	end
	if i != 1
		while i <= 5
			tags[i] << ''
			i += 1
		end
	end
end





tags2 = []
people = []
tags2 << [PUBLIC_TITLE]
DB.fetch("select g.id i, g.name n, gg.average_people a from grades_tags gg, tags g where gg.tag_id = g.id and gg.number > 5 and g.name not like '%Cannes%' order by average_people desc limit 5").each do |row|
	tags2 << [record_public(row)]
	people << row[:i]
end

tags2[0] << [PRESS_TITLE]
press = []
i = 1
DB.fetch("select g.id i, g.name n, gg.average_press a from grades_tags gg, tags g where gg.tag_id = g.id and gg.number > 5 and g.name not like '%Cannes%' order by average_press desc limit 5").each do |row|
	tags2[i] << record_press(row, people)
	press << row[:i]
	i += 1
end

DB.fetch('select name n, id i from critics order by critics.name').each do |r1|
	i = 1
	DB.fetch("select g.id i, g.name n, gc.average a from tags_per_critics gc, tags g where gc.critic_id = ? and g.id = gc.tag_id and gc.number > 5 and g.name not like '%Cannes%' order by average desc limit 5", r1[:i]) do |r2|
		if i == 1
			tags2[0] << r1[:n]
		end
		tags2[i] << record_critic(r2, people, press)
		i += 1
	end
	if i != 1
		while i <= 5
			tags2[i] << ''
			i += 1
		end
	end
end

template = ERB.new File.new("process/export.erb.html").read, nil, "%"
puts template.result(binding)
