#!/usr/bin/env ruby
require 'twitter'
require 'open-uri'

client = Twitter::Streaming::Client.new do |config|
  config.consumer_key        = "MJRjlMVEkrhif2nypcNnuneb2"
  config.consumer_secret     = "F3PVyvO3piM4dwmROfApEpDyPh6dO7lFv9ED4Ta5B2Pumsyd7E"
  config.access_token        = "4861176627-jpIqZaAohpCVTV365muaZjeumfqan0g6u0swtx9"
  config.access_token_secret = "SSq5kvEPys4T2x6HGVrYMFstMxwwJw6le7EoA0kGD3mZ5"
end

client.user do |object|
  case object
  when Twitter::Tweet
    object.media.each do |media|
      url = media.media_url
      puts "Downloading #{url.to_s}..."
      open("images/#{url.basename}", 'wb') do |file|
        file << open(url.to_s).read
      end
    end
  end
end
