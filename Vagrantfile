# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure("2") do |config|
  config.vm.box = "ubuntu/xenial64"
  # Be sure to have the following vagrant plugin installed.
  # vagrant plugin install vagrant-vbguest
  config.vm.synced_folder ".", "/vagrant"

  config.vm.provider "virtualbox" do |vb|
    vb.gui = false
    vb.memory = "512"
 end

  config.vm.define "cookjson" do |cookjson|
    cookjson.vm.hostname = "cookjson"
    cookjson.vm.network "private_network", ip: "10.2.3.10"
	cookjson.vm.provision :shell, inline: "apt-get install -y openjdk-8-jre mongodb"
   end
end
