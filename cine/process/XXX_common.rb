require 'database'

PEOPLE_CLASS = 'people'
PRESS_CLASS = 'press'
PUBLIC_TITLE= "<span class='#{PEOPLE_CLASS}'>Public</span>"
PRESS_TITLE= "<span class='#{PRESS_CLASS}'>Presse</span>"

def to_table data, out
  data.each do |r|
    while r.length < 11
      r << ''
    end
  end
  data = data.transpose

  out << "<table>\n"
  data.each do |l|
    out << "<tr><td>#{l.join('</td><td>')}</td><tr>\n"
  end
  out << "</table>\n"

end

