/*
 * This file is a part of project QuickShop, the name is ReleaseJsonContainer.java
 * Copyright (C) Ghost_chu <https://github.com/Ghost-chu>
 * Copyright (C) Bukkit Commons Studio and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.maxgamer.quickshop.Util.Github;

import java.util.List;

public class ReleaseJsonContainer {
    /**
     * url : https://api.github.com/repos/octocat/Hello-World/releases/1 html_url :
     * https://github.com/octocat/Hello-World/releases/v1.0.0 assets_url :
     * https://api.github.com/repos/octocat/Hello-World/releases/1/assets upload_url :
     * https://uploads.github.com/repos/octocat/Hello-World/releases/1/assets{?name,label} tarball_url
     * : https://api.github.com/repos/octocat/Hello-World/tarball/v1.0.0 zipball_url :
     * https://api.github.com/repos/octocat/Hello-World/zipball/v1.0.0 id : 1 node_id :
     * MDc6UmVsZWFzZTE= tag_name : v1.0.0 target_commitish : master name : v1.0.0 body : Description
     * of the release draft : false prerelease : false created_at : 2013-02-27T19:35:32Z published_at
     * : 2013-02-27T19:35:32Z author :
     * {"login":"octocat","id":1,"node_id":"MDQ6VXNlcjE=","avatar_url":"https://github.com/images/error/octocat_happy.gif","gravatar_id":"","url":"https://api.github.com/users/octocat","html_url":"https://github.com/octocat","followers_url":"https://api.github.com/users/octocat/followers","following_url":"https://api.github.com/users/octocat/following{/other_user}","gists_url":"https://api.github.com/users/octocat/gists{/gist_id}","starred_url":"https://api.github.com/users/octocat/starred{/owner}{/repo}","subscriptions_url":"https://api.github.com/users/octocat/subscriptions","organizations_url":"https://api.github.com/users/octocat/orgs","repos_url":"https://api.github.com/users/octocat/repos","events_url":"https://api.github.com/users/octocat/events{/privacy}","received_events_url":"https://api.github.com/users/octocat/received_events","type":"User","site_admin":false}
     * assets :
     * [{"url":"https://api.github.com/repos/octocat/Hello-World/releases/assets/1","browser_download_url":"https://github.com/octocat/Hello-World/releases/download/v1.0.0/example.zip","id":1,"node_id":"MDEyOlJlbGVhc2VBc3NldDE=","name":"example.zip","label":"short
     * description","state":"uploaded","content_type":"application/zip","size":1024,"download_count":42,"created_at":"2013-02-27T19:35:32Z","updated_at":"2013-02-27T19:35:32Z","uploader":{"login":"octocat","id":1,"node_id":"MDQ6VXNlcjE=","avatar_url":"https://github.com/images/error/octocat_happy.gif","gravatar_id":"","url":"https://api.github.com/users/octocat","html_url":"https://github.com/octocat","followers_url":"https://api.github.com/users/octocat/followers","following_url":"https://api.github.com/users/octocat/following{/other_user}","gists_url":"https://api.github.com/users/octocat/gists{/gist_id}","starred_url":"https://api.github.com/users/octocat/starred{/owner}{/repo}","subscriptions_url":"https://api.github.com/users/octocat/subscriptions","organizations_url":"https://api.github.com/users/octocat/orgs","repos_url":"https://api.github.com/users/octocat/repos","events_url":"https://api.github.com/users/octocat/events{/privacy}","received_events_url":"https://api.github.com/users/octocat/received_events","type":"User","site_admin":false}}]
     */
    private int id;

    private String name;

    private List<AssetsBean> assets;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<AssetsBean> getAssets() {
        return assets;
    }

    public static class AssetsBean {
        /**
         * url : https://api.github.com/repos/octocat/Hello-World/releases/assets/1 browser_download_url
         * : https://github.com/octocat/Hello-World/releases/download/v1.0.0/example.zip id : 1 node_id
         * : MDEyOlJlbGVhc2VBc3NldDE= name : example.zip label : short description state : uploaded
         * content_type : application/zip size : 1024 download_count : 42 created_at :
         * 2013-02-27T19:35:32Z updated_at : 2013-02-27T19:35:32Z uploader :
         * {"login":"octocat","id":1,"node_id":"MDQ6VXNlcjE=","avatar_url":"https://github.com/images/error/octocat_happy.gif","gravatar_id":"","url":"https://api.github.com/users/octocat","html_url":"https://github.com/octocat","followers_url":"https://api.github.com/users/octocat/followers","following_url":"https://api.github.com/users/octocat/following{/other_user}","gists_url":"https://api.github.com/users/octocat/gists{/gist_id}","starred_url":"https://api.github.com/users/octocat/starred{/owner}{/repo}","subscriptions_url":"https://api.github.com/users/octocat/subscriptions","organizations_url":"https://api.github.com/users/octocat/orgs","repos_url":"https://api.github.com/users/octocat/repos","events_url":"https://api.github.com/users/octocat/events{/privacy}","received_events_url":"https://api.github.com/users/octocat/received_events","type":"User","site_admin":false}
         */
        private String browser_download_url;

        private String name;

        private int size;

        public AssetsBean() {
        }

        public String getBrowser_download_url() {
            return browser_download_url;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

    }

}
