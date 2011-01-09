PARSING_REGEX = /[^#]+#(\S*).*/

# identify_main_colors(image, colors_number)
#
# Identify the main colors of an image using image magic
# pass the image path and the number of colors to identify
def identify_main_colors image_url, colors_number
  text   = "/usr/local/bin/convert #{image_url} -colors #{colors_number} -unique-colors txt:"
  result = IO.popen(text) { |pipe| pipe.readlines }
  if $?.success?
    if result.length > 1
      colors = []
      1.upto(result.length - 1) do |i|
        if m = PARSING_REGEX.match(result[i])
          colors << m.captures[0]
        else
          STDERR << "Error parsing result of #{image_url}\n"
        end
      end
      colors
    else
      STDERR << "Error parsing #{image_url}\n"
      nil
    end
  else
    STDERR << "Error parsing #{image_url}\n"
    nil
  end
end

def color_to_number color
  [color[0, 2].hex, color[2, 2].hex, color[4, 2].hex]
end

def distance color1, color2
  (color1[0] - color2[0]) ** 2 +  (color1[1] - color2[1]) ** 2 + (color1[2] - color2[2]) ** 2
end

def pixels_to_image pixels
  result = "# ImageMagick pixel enumeration: #{pixels.length},1,255,rgb\n"
  pixels.each_index do |i|
    result << "#{i},0: (#{color_to_number(pixels[i]).join(',')})\n"
  end
  result
end