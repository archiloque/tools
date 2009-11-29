# This script read a file whose name is in param
# Each line of this file should contains an url
# The content of these url will be donwloaded to the current directory using
# the typhoeus gem at http://github.com/abhay/typhoeus/tree/master

require 'rubygems'
require 'typhoeus'
require 'uri'
require 'parsedate'
require 'time'

if ARGV.length < 1
  raise "Missing file name argument"
end

def parse_header header
  result = {}
  header.each_line do |line|
    if 0 == line.index('Last-Modified:')
      result['last_modified'] = Time.httpdate(line['Last-Modified:'.length .. line.length].strip)
    end
  end
  result
end

module Typhoeus
  class Colossus
    def initialize
      @multi       = Multi.new
      @easy_pool   = []
    end

    def queue(request)
      @multi.add(get_easy_object(request))
    end

    def get_easy_object(request)
      easy = Easy.new #@easy_pool.pop || Easy.new
      easy.url          = request.url
      easy.method       = request.method
      easy.headers      = request.headers if request.headers
      easy.request_body = request.body    if request.body
      easy.timeout      = request.timeout if request.timeout
      easy.on_success do |easy|
        request.response = response_from_easy(easy)
        request.call_handlers
#        @easy_pool.push(easy)
      end
      easy.on_failure do |easy|
        request.response = response_from_easy(easy)
        request.call_handlers
#        @easy_pool.push(easy)
      end
      easy.set_headers
      easy
    end

    def response_from_easy(easy)
      Response.new(:code    => easy.response_code,
                   :headers => easy.response_header,
                   :body    => easy.response_body,
                   :time    => easy.total_time_taken,
                   :requested_url => easy.url)
    end

    def run
      @multi.perform
    end
  end
end


hydra = Typhoeus::Colossus.new

File.open(ARGV[0]) do |file|
  while (line = file.gets)
    file_name = line.chomp
    unless file_name.empty?
      path = URI::parse(file_name).path
      target_name = path[(path.rindex("/")+1) .. (path.length)]
      puts "Downloading [#{file_name}]"
      headers = {}
      if File.exist?(target_name)
        headers['If-Modified-Since'] = File.mtime(target_name).httpdate
      end
      request = Typhoeus::Request.new(:method => :get, :host => '', :path => file_name, :headers => headers)
      request.on_complete do |response|
        path = URI::parse(response.requested_url).path
        target_name = path[(path.rindex("/")+1) .. (path.length)]
        if response.code == 200
          headers = parse_header(response.headers)
          last_modified = headers['last_modified']
          print "File [#{target_name}] updated on server\n"
          File.open(target_name, 'w') {|f| f.write(response.body) }
          if last_modified
            File.utime(last_modified, last_modified, target_name)
          end
        elsif response.code == 304
          print "File [#{target_name}] up to date\n"
        else
          print "Response code #{response.code} for file [#{target_name}]\n"
        end
      end
      hydra.queue(request)
    end
  end
end

hydra.run
