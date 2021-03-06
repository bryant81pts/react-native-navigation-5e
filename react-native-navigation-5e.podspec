require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-navigation-5e"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = "https://github.com/Project5E/react-native-navigation-5e"
  s.license      = package["license"]
  s.authors      = package["author"]

  s.platforms    = { :ios => "11.0" }
  s.source       = { :git => "https://github.com/Alice-Theresa/react-native-navigation-5e.git", :tag => "#{s.version}" }

  
  s.source_files = "ios/**/*.{h,m,mm}"
  

  s.dependency "React"
end
