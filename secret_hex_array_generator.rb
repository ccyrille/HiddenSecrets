#!/usr/bin/env ruby

require "digest"
require "byebug"

if ARGV.length < 2 || ARGV.any?(&:empty?)
    puts <<~TEXT
        Secret to be hidden should be provided as ARGV[0]
        Android app package name should be provided as ARGV[1]
    TEXT
    exit 1
end

secret_key = ARGV[0]
package_name =  ARGV[1]

secret_key_bytes = secret_key.bytes

def char_hex_array(bytes)
  bytes.map{|b| "0x" + b.to_s(16) }
end

obfuscator = Digest::SHA256.hexdigest(package_name)
obfuscator_bytes = obfuscator.bytes
obfuscator_size = obfuscator.size

obfuscated_secret_bytes = []
secret_key.bytes.each_with_index do |secret_byte, i|
  obfuscated_byte = secret_byte ^ obfuscator_bytes[i % obfuscator_size]
  obfuscated_secret_bytes.append(obfuscated_byte)
end


puts <<~TEXT
    ### KEY ###
    #{secret_key}

    ### PACKAGE NAME ###
    #{package_name}

    ### OBFUSCATED KEY ###
    { #{char_hex_array(obfuscated_secret_bytes).join(", ")} }
TEXT
