require 'rubygems'
require 'database'
require 'nokogiri'
require 'open-uri'

current_date = Date.today
# months_number = 120 # 120
films_number = 0
DB.transaction do
  while current_date.strftime("%Y-%m") != '1999-12'
#  1.upto(months_number) do
    page_url = "http://www.allocine.fr/film/agenda_mois.html?month=#{current_date.strftime("%Y-%m")}"
    p page_url
    doc = Nokogiri::HTML(open(page_url), page_url, 'UTF-8')
    doc.search('.vmargin20b').each do |liste|
      day_in_month = liste.parent.search('h2')[0].text[0...2].strip.to_i
      # invalid date
      unless day_in_month == 0
        actual_date = Date.new(current_date.year, current_date.month, day_in_month)
        liste.search('.bold a').each do |f|
          title = f.text.strip
          href = f[:href]
          custom_id = href[(href.index('=')+1)..(href.index('.') - 1)].to_i
          unless Film.first(:custom_id => custom_id)
	          Film.create(:title => title, :custom_id => custom_id, :pub_date => actual_date)
    	      films_number += 1
    	  end
        end
      end
    end
    current_date = current_date << 1
  end
end
p "#{films_number} films found"