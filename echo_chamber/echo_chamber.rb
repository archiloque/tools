# Calculate statistics for your twitter account:
# - the number of your followers that follow the people you follow
# - people followed by your follower that you don't follow
# - people followed by people you follow that you don't follow
#
# It requires a twitter application id (see https://twitter.com/apps to create one) it's needed to access some APIs and
# to increase the number of allowed querries.
#
# Even in this case the number of queries is low, so when the limit is reached all the data are dumped to
# a your_profile_name.json file and the script stop.
# Next time the script is launched with the same accoutn information it acess the file and goes on till it finish.

require 'rubygems'
require 'oauth'
require 'json'
require 'erb'

class TwitterClient

  attr_accessor :block_when_fail

  def initialize access_token
    @access_token = access_token
  end

  def show_user user_id
    unless user_id
      raise 'No user_id !'
    end
    p "Fetching user #{user_id}"
    get_json "http://api.twitter.com/1/users/show.json?user_id=#{user_id}"
  end

  def show_users users
    p "Fetching profiles for #{users.size} users"
    result = []
    unless users.size < 100
      0.upto((users.size / 100) - 1) do |i|
        result.concat(get_json("http://api.twitter.com/1/users/lookup.json?user_id=#{users[i * 100, 100].join(',')}"))
      end
    end

    left = (users.size % 100)

    unless left == 0
      result.concat(get_json("http://api.twitter.com/1/users/lookup.json?user_id=#{users[-left, left].join(',')}"))
    end
    p "#{result.size} profiles found"
    result
  end

  def followers user_id
    unless user_id
      raise 'No user_id !'
    end
    p "Fetching followers of #{user_id}"
    cursor = -1
    ids = []
    while cursor != 0
      data = get_json("http://api.twitter.com/1/followers/ids.json?user_id=#{user_id}&cursor=#{cursor}")
      if data
        cursor = data['next_cursor']
        ids.concat(data['ids'])
      else
        cursor = 0
      end
    end
    p "#{ids.size} followers found"
    ids.collect { |i| i.to_s }
  end

  def friends user_id
    unless user_id
      raise 'No user_id !'
    end
    p "Fetching friends of #{user_id}"
    cursor = -1
    ids = []
    while cursor != 0
      data = get_json("http://api.twitter.com/1/friends/ids.json?user_id=#{user_id}&cursor=#{cursor}")
      if data
        cursor = data['next_cursor']
        ids.concat(data['ids'])
      else
        p 'Profile is private'
        cursor = 0
      end
    end
    p "#{ids.size} friends found"
    ids.collect { |i| i.to_s }
  end

  private

  def get_json url
    c = @access_token.get(url)
    if c.code == '200'
      JSON.parse(c.body)
    elsif c.code == '400'
      block_when_fail.call
      raise 'Rate limit reached, data saved, please relaunch the script later to go on its execution'
    elsif c.code == '401'
      nil
    else
      raise c.message
    end
  end

end

unless ARGV.size == 2
  raise 'Please use your application consumer key and consumer secret as parameter'
end

oauth = OAuth::Consumer.new(ARGV[0], ARGV[1], {
        :request_token_url => 'https://twitter.com/oauth/request_token',
        :authorize_url     => 'https://twitter.com/oauth/authorize',
        :access_token_url  => 'https://twitter.com/oauth/access_token',
        :site => 'https://twitter.com'
})

request_token = oauth.get_request_token

puts "Please go to #{request_token.authorize_url} and return the pin code (you may have to login)"

access_token = request_token.get_access_token(:oauth_verifier => gets.chomp)

screen_name = access_token.params['screen_name']
output_file_name = "#{screen_name}.json"
user_id = access_token.params['user_id']

p "User to check is #{screen_name} for id #{user_id}"

# Loading existing data or initializing
if File.exists? output_file_name
  p "** Loading existing data from #{output_file_name}"
  data = JSON.parse(IO.read(output_file_name))
  ecc = TwitterClient.new(access_token)
else
  data = {'screen_name' => screen_name, 'user_id' => user_id}
  ecc = TwitterClient.new(access_token)
  data['users'] = {user_id => {}}
end

save_data = lambda do
  File.open(output_file_name, 'w') do |f|
    f.write(data.to_json)
  end
end

ecc.block_when_fail= save_data

unless followers = data['users'][user_id]['followers']
  p "** Fetching your followers"
  followers = data['users'][user_id]['followers'] = ecc.followers(user_id)
end


p "** Will fetch who your #{followers.size} followers are following"
followers.each do |id|
  user = data['users'][id]

  unless user
    user = data['users'][id] = {}
  end

  if user['friends']
    p "Friends of #{id} already fetched"
  else
    user['friends'] = ecc.friends(id)
  end
end

unless friends = data['users'][user_id]['friends']
  p "** Fetching people you follow"
  friends = data['users'][user_id]['friends'] = ecc.friends(user_id)
end

p "** Will fetch who the #{friends.size} people you follow are following"
friends.each do |id|
  user = data['users'][id]
  unless user
    user = data['users'][id] = {}
  end
  if user['friends']
    p "Friends of #{id} already fetched"
  else
    user['friends'] = ecc.friends(id)
  end
end

profiles_to_fetch = [user_id] + friends

friends_of_friends = Hash.new { |hash, key| hash[key] = 0 }
friends.each do |friend_id|
  data['users'][friend_id]['friends'].each do |f|
    friends_of_friends[f] += 1
  end
end

followed_by_friends = friends_of_friends.keys.reject{|i| friends.include?(i) || (i == user_id) }.sort{|a,b| friends_of_friends[b] <=> friends_of_friends[a]}
followed_by_friends = followed_by_friends[0...[20, followed_by_friends.size].min]

profiles_to_fetch.concat followed_by_friends

friends_of_followers = Hash.new { |hash, key| hash[key] = 0 }
followers.each do |follower_id|
  data['users'][follower_id]['friends'].each do |f|
    friends_of_followers[f] += 1
  end
end

followed_by_followers = friends_of_followers.keys.reject{|i| friends.include?(i) || (i == user_id) }.sort{|a,b| friends_of_followers[b] <=> friends_of_followers[a]}
followed_by_followers = followed_by_followers[0...[20, followed_by_followers.size].min]

profiles_to_fetch.concat followed_by_followers

p "** Will fetch the profiles"
profiles_to_fetch = profiles_to_fetch.uniq.delete_if do |i|
  delete = data['users'][i] && data['users'][i]['profile']
  if delete
    p "#{i} already fetched"
  end
  delete
end

ecc.show_users(profiles_to_fetch).each do |pro|
  id = pro['id'].to_s
  unless data['users'][id]
    data['users'][id] = {}
  end
  data['users'][id]['profile'] = pro
end

save_data.call

p "Creating display"

User = Struct.new(:name, :screen_name, :image_url, :nb_followers, :nb_followed)

def user_from_profile user, friends_of_friends, friends_of_followers
  User.new(user['name'], user['screen_name'], user['profile_image_url'], friends_of_friends[user['id'].to_s], friends_of_followers[user['id'].to_s])
end

friends_users = data['users'][user_id]['friends'].collect do |i|
  user = data['users'][i]['profile']
  user_from_profile(user, friends_of_friends, friends_of_followers)
end.sort do |x, y|
  if x.nb_followers == y.nb_followers
    y.nb_followed - x.nb_followed
  else
    y.nb_followers - x.nb_followers
  end
end

followed_by_friends.collect!{|i| user_from_profile(data['users'][i]['profile'], friends_of_friends, friends_of_followers)}
followed_by_followers.collect!{|i| user_from_profile(data['users'][i]['profile'], friends_of_friends, friends_of_followers)}

class TemplateParams

  def initialize user_name, user_profile_image_url, user_friends_count, user_followers_count, user_followed_that_follow, followers, followed_by_friends, followed_by_followers
    @user_name = user_name
    @user_profile_image_url = user_profile_image_url
    @user_friends_count = user_friends_count
    @user_followers_count = user_followers_count
    @user_followed_that_follow = user_followed_that_follow
    @followers = followers
    @followed_by_friends = followed_by_friends
    @followed_by_followers = followed_by_followers
  end

  def get_binding
    binding
  end

end

File.open("#{screen_name}.html", 'w') do |f|
  user = data['users'][user_id]['profile']
  params = TemplateParams.new(user['screen_name'], user['profile_image_url'], user['friends_count'], user['followers_count'], (friends & followers).size, friends_users, followed_by_friends, followed_by_followers)
  result = ERB.new(IO.read('template.html.erb')).result(params.get_binding)
  f.write result
end

p "Result is available in #{#{screen_name}.html}"
