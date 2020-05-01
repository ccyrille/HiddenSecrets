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

secret = ARGV[0]
package_name =  ARGV[1]

def char_hex_array(bytes)
  bytes.map{|b| "0x" + b.to_s(16) }
end

# Generate the obfuscator as the SHA256 of the app package name
obfuscator = Digest::SHA256.hexdigest(package_name)
obfuscator_bytes = obfuscator.bytes
obfuscator_size = obfuscator.size

# Generate the obfuscated secret bytes array by applying a XOR
# between the secret and the obfuscator
obfuscated_secret_bytes = []
secret.bytes.each_with_index do |secret_byte, i|
  obfuscated_byte = secret_byte ^ obfuscator_bytes[i % obfuscator_size]
  obfuscated_secret_bytes.append(obfuscated_byte)
end


puts <<~TEXT
    ### KEY ###
    #{secret_key}

    ### PACKAGE NAME ###
    #{package_name}

    ### OBFUSCATED SECRET ###
    { #{char_hex_array(obfuscated_secret_bytes).join(", ")} }
TEXT
