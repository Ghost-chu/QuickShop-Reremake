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
     * url : https://api.github.com/repos/octocat/Hello-World/releases/1
     * html_url : https://github.com/octocat/Hello-World/releases/v1.0.0
     * assets_url : https://api.github.com/repos/octocat/Hello-World/releases/1/assets
     * upload_url : https://uploads.github.com/repos/octocat/Hello-World/releases/1/assets{?name,label}
     * tarball_url : https://api.github.com/repos/octocat/Hello-World/tarball/v1.0.0
     * zipball_url : https://api.github.com/repos/octocat/Hello-World/zipball/v1.0.0
     * id : 1
     * node_id : MDc6UmVsZWFzZTE=
     * tag_name : v1.0.0
     * target_commitish : master
     * name : v1.0.0
     * body : Description of the release
     * draft : false
     * prerelease : false
     * created_at : 2013-02-27T19:35:32Z
     * published_at : 2013-02-27T19:35:32Z
     * author : {"login":"octocat","id":1,"node_id":"MDQ6VXNlcjE=","avatar_url":"https://github.com/images/error/octocat_happy.gif","gravatar_id":"","url":"https://api.github.com/users/octocat","html_url":"https://github.com/octocat","followers_url":"https://api.github.com/users/octocat/followers","following_url":"https://api.github.com/users/octocat/following{/other_user}","gists_url":"https://api.github.com/users/octocat/gists{/gist_id}","starred_url":"https://api.github.com/users/octocat/starred{/owner}{/repo}","subscriptions_url":"https://api.github.com/users/octocat/subscriptions","organizations_url":"https://api.github.com/users/octocat/orgs","repos_url":"https://api.github.com/users/octocat/repos","events_url":"https://api.github.com/users/octocat/events{/privacy}","received_events_url":"https://api.github.com/users/octocat/received_events","type":"User","site_admin":false}
     * assets : [{"url":"https://api.github.com/repos/octocat/Hello-World/releases/assets/1","browser_download_url":"https://github.com/octocat/Hello-World/releases/download/v1.0.0/example.zip","id":1,"node_id":"MDEyOlJlbGVhc2VBc3NldDE=","name":"example.zip","label":"short description","state":"uploaded","content_type":"application/zip","size":1024,"download_count":42,"created_at":"2013-02-27T19:35:32Z","updated_at":"2013-02-27T19:35:32Z","uploader":{"login":"octocat","id":1,"node_id":"MDQ6VXNlcjE=","avatar_url":"https://github.com/images/error/octocat_happy.gif","gravatar_id":"","url":"https://api.github.com/users/octocat","html_url":"https://github.com/octocat","followers_url":"https://api.github.com/users/octocat/followers","following_url":"https://api.github.com/users/octocat/following{/other_user}","gists_url":"https://api.github.com/users/octocat/gists{/gist_id}","starred_url":"https://api.github.com/users/octocat/starred{/owner}{/repo}","subscriptions_url":"https://api.github.com/users/octocat/subscriptions","organizations_url":"https://api.github.com/users/octocat/orgs","repos_url":"https://api.github.com/users/octocat/repos","events_url":"https://api.github.com/users/octocat/events{/privacy}","received_events_url":"https://api.github.com/users/octocat/received_events","type":"User","site_admin":false}}]
     */

    private String url;
    private String html_url;
    private String assets_url;
    private String upload_url;
    private String tarball_url;
    private String zipball_url;
    private int id;
    private String node_id;
    private String tag_name;
    private String target_commitish;
    private String name;
    private String body;
    private boolean draft;
    private boolean prerelease;
    private String created_at;
    private String published_at;
    private AuthorBean author;
    private List<AssetsBean> assets;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHtml_url() {
        return html_url;
    }

    public void setHtml_url(String html_url) {
        this.html_url = html_url;
    }

    public String getAssets_url() {
        return assets_url;
    }

    public void setAssets_url(String assets_url) {
        this.assets_url = assets_url;
    }

    public String getUpload_url() {
        return upload_url;
    }

    public void setUpload_url(String upload_url) {
        this.upload_url = upload_url;
    }

    public String getTarball_url() {
        return tarball_url;
    }

    public void setTarball_url(String tarball_url) {
        this.tarball_url = tarball_url;
    }

    public String getZipball_url() {
        return zipball_url;
    }

    public void setZipball_url(String zipball_url) {
        this.zipball_url = zipball_url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNode_id() {
        return node_id;
    }

    public void setNode_id(String node_id) {
        this.node_id = node_id;
    }

    public String getTag_name() {
        return tag_name;
    }

    public void setTag_name(String tag_name) {
        this.tag_name = tag_name;
    }

    public String getTarget_commitish() {
        return target_commitish;
    }

    public void setTarget_commitish(String target_commitish) {
        this.target_commitish = target_commitish;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isDraft() {
        return draft;
    }

    public void setDraft(boolean draft) {
        this.draft = draft;
    }

    public boolean isPrerelease() {
        return prerelease;
    }

    public void setPrerelease(boolean prerelease) {
        this.prerelease = prerelease;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getPublished_at() {
        return published_at;
    }

    public void setPublished_at(String published_at) {
        this.published_at = published_at;
    }

    public AuthorBean getAuthor() {
        return author;
    }

    public void setAuthor(AuthorBean author) {
        this.author = author;
    }

    public List<AssetsBean> getAssets() {
        return assets;
    }

    public void setAssets(List<AssetsBean> assets) {
        this.assets = assets;
    }

    public static class AuthorBean {
        /**
         * login : octocat
         * id : 1
         * node_id : MDQ6VXNlcjE=
         * avatar_url : https://github.com/images/error/octocat_happy.gif
         * gravatar_id :
         * url : https://api.github.com/users/octocat
         * html_url : https://github.com/octocat
         * followers_url : https://api.github.com/users/octocat/followers
         * following_url : https://api.github.com/users/octocat/following{/other_user}
         * gists_url : https://api.github.com/users/octocat/gists{/gist_id}
         * starred_url : https://api.github.com/users/octocat/starred{/owner}{/repo}
         * subscriptions_url : https://api.github.com/users/octocat/subscriptions
         * organizations_url : https://api.github.com/users/octocat/orgs
         * repos_url : https://api.github.com/users/octocat/repos
         * events_url : https://api.github.com/users/octocat/events{/privacy}
         * received_events_url : https://api.github.com/users/octocat/received_events
         * type : User
         * site_admin : false
         */

        private String login;
        private int id;
        private String node_id;
        private String avatar_url;
        private String gravatar_id;
        private String url;
        private String html_url;
        private String followers_url;
        private String following_url;
        private String gists_url;
        private String starred_url;
        private String subscriptions_url;
        private String organizations_url;
        private String repos_url;
        private String events_url;
        private String received_events_url;
        private String type;
        private boolean site_admin;

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getNode_id() {
            return node_id;
        }

        public void setNode_id(String node_id) {
            this.node_id = node_id;
        }

        public String getAvatar_url() {
            return avatar_url;
        }

        public void setAvatar_url(String avatar_url) {
            this.avatar_url = avatar_url;
        }

        public String getGravatar_id() {
            return gravatar_id;
        }

        public void setGravatar_id(String gravatar_id) {
            this.gravatar_id = gravatar_id;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getHtml_url() {
            return html_url;
        }

        public void setHtml_url(String html_url) {
            this.html_url = html_url;
        }

        public String getFollowers_url() {
            return followers_url;
        }

        public void setFollowers_url(String followers_url) {
            this.followers_url = followers_url;
        }

        public String getFollowing_url() {
            return following_url;
        }

        public void setFollowing_url(String following_url) {
            this.following_url = following_url;
        }

        public String getGists_url() {
            return gists_url;
        }

        public void setGists_url(String gists_url) {
            this.gists_url = gists_url;
        }

        public String getStarred_url() {
            return starred_url;
        }

        public void setStarred_url(String starred_url) {
            this.starred_url = starred_url;
        }

        public String getSubscriptions_url() {
            return subscriptions_url;
        }

        public void setSubscriptions_url(String subscriptions_url) {
            this.subscriptions_url = subscriptions_url;
        }

        public String getOrganizations_url() {
            return organizations_url;
        }

        public void setOrganizations_url(String organizations_url) {
            this.organizations_url = organizations_url;
        }

        public String getRepos_url() {
            return repos_url;
        }

        public void setRepos_url(String repos_url) {
            this.repos_url = repos_url;
        }

        public String getEvents_url() {
            return events_url;
        }

        public void setEvents_url(String events_url) {
            this.events_url = events_url;
        }

        public String getReceived_events_url() {
            return received_events_url;
        }

        public void setReceived_events_url(String received_events_url) {
            this.received_events_url = received_events_url;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isSite_admin() {
            return site_admin;
        }

        public void setSite_admin(boolean site_admin) {
            this.site_admin = site_admin;
        }
    }

    public static class AssetsBean {
        /**
         * url : https://api.github.com/repos/octocat/Hello-World/releases/assets/1
         * browser_download_url : https://github.com/octocat/Hello-World/releases/download/v1.0.0/example.zip
         * id : 1
         * node_id : MDEyOlJlbGVhc2VBc3NldDE=
         * name : example.zip
         * label : short description
         * state : uploaded
         * content_type : application/zip
         * size : 1024
         * download_count : 42
         * created_at : 2013-02-27T19:35:32Z
         * updated_at : 2013-02-27T19:35:32Z
         * uploader : {"login":"octocat","id":1,"node_id":"MDQ6VXNlcjE=","avatar_url":"https://github.com/images/error/octocat_happy.gif","gravatar_id":"","url":"https://api.github.com/users/octocat","html_url":"https://github.com/octocat","followers_url":"https://api.github.com/users/octocat/followers","following_url":"https://api.github.com/users/octocat/following{/other_user}","gists_url":"https://api.github.com/users/octocat/gists{/gist_id}","starred_url":"https://api.github.com/users/octocat/starred{/owner}{/repo}","subscriptions_url":"https://api.github.com/users/octocat/subscriptions","organizations_url":"https://api.github.com/users/octocat/orgs","repos_url":"https://api.github.com/users/octocat/repos","events_url":"https://api.github.com/users/octocat/events{/privacy}","received_events_url":"https://api.github.com/users/octocat/received_events","type":"User","site_admin":false}
         */

        private String url;
        private String browser_download_url;
        private int id;
        private String node_id;
        private String name;
        private String label;
        private String state;
        private String content_type;
        private int size;
        private int download_count;
        private String created_at;
        private String updated_at;
        private UploaderBean uploader;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getBrowser_download_url() {
            return browser_download_url;
        }

        public void setBrowser_download_url(String browser_download_url) {
            this.browser_download_url = browser_download_url;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getNode_id() {
            return node_id;
        }

        public void setNode_id(String node_id) {
            this.node_id = node_id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getContent_type() {
            return content_type;
        }

        public void setContent_type(String content_type) {
            this.content_type = content_type;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public int getDownload_count() {
            return download_count;
        }

        public void setDownload_count(int download_count) {
            this.download_count = download_count;
        }

        public String getCreated_at() {
            return created_at;
        }

        public void setCreated_at(String created_at) {
            this.created_at = created_at;
        }

        public String getUpdated_at() {
            return updated_at;
        }

        public void setUpdated_at(String updated_at) {
            this.updated_at = updated_at;
        }

        public UploaderBean getUploader() {
            return uploader;
        }

        public void setUploader(UploaderBean uploader) {
            this.uploader = uploader;
        }

        public static class UploaderBean {
            /**
             * login : octocat
             * id : 1
             * node_id : MDQ6VXNlcjE=
             * avatar_url : https://github.com/images/error/octocat_happy.gif
             * gravatar_id :
             * url : https://api.github.com/users/octocat
             * html_url : https://github.com/octocat
             * followers_url : https://api.github.com/users/octocat/followers
             * following_url : https://api.github.com/users/octocat/following{/other_user}
             * gists_url : https://api.github.com/users/octocat/gists{/gist_id}
             * starred_url : https://api.github.com/users/octocat/starred{/owner}{/repo}
             * subscriptions_url : https://api.github.com/users/octocat/subscriptions
             * organizations_url : https://api.github.com/users/octocat/orgs
             * repos_url : https://api.github.com/users/octocat/repos
             * events_url : https://api.github.com/users/octocat/events{/privacy}
             * received_events_url : https://api.github.com/users/octocat/received_events
             * type : User
             * site_admin : false
             */

            private String login;
            private int id;
            private String node_id;
            private String avatar_url;
            private String gravatar_id;
            private String url;
            private String html_url;
            private String followers_url;
            private String following_url;
            private String gists_url;
            private String starred_url;
            private String subscriptions_url;
            private String organizations_url;
            private String repos_url;
            private String events_url;
            private String received_events_url;
            private String type;
            private boolean site_admin;

            public String getLogin() {
                return login;
            }

            public void setLogin(String login) {
                this.login = login;
            }

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getNode_id() {
                return node_id;
            }

            public void setNode_id(String node_id) {
                this.node_id = node_id;
            }

            public String getAvatar_url() {
                return avatar_url;
            }

            public void setAvatar_url(String avatar_url) {
                this.avatar_url = avatar_url;
            }

            public String getGravatar_id() {
                return gravatar_id;
            }

            public void setGravatar_id(String gravatar_id) {
                this.gravatar_id = gravatar_id;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public String getHtml_url() {
                return html_url;
            }

            public void setHtml_url(String html_url) {
                this.html_url = html_url;
            }

            public String getFollowers_url() {
                return followers_url;
            }

            public void setFollowers_url(String followers_url) {
                this.followers_url = followers_url;
            }

            public String getFollowing_url() {
                return following_url;
            }

            public void setFollowing_url(String following_url) {
                this.following_url = following_url;
            }

            public String getGists_url() {
                return gists_url;
            }

            public void setGists_url(String gists_url) {
                this.gists_url = gists_url;
            }

            public String getStarred_url() {
                return starred_url;
            }

            public void setStarred_url(String starred_url) {
                this.starred_url = starred_url;
            }

            public String getSubscriptions_url() {
                return subscriptions_url;
            }

            public void setSubscriptions_url(String subscriptions_url) {
                this.subscriptions_url = subscriptions_url;
            }

            public String getOrganizations_url() {
                return organizations_url;
            }

            public void setOrganizations_url(String organizations_url) {
                this.organizations_url = organizations_url;
            }

            public String getRepos_url() {
                return repos_url;
            }

            public void setRepos_url(String repos_url) {
                this.repos_url = repos_url;
            }

            public String getEvents_url() {
                return events_url;
            }

            public void setEvents_url(String events_url) {
                this.events_url = events_url;
            }

            public String getReceived_events_url() {
                return received_events_url;
            }

            public void setReceived_events_url(String received_events_url) {
                this.received_events_url = received_events_url;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public boolean isSite_admin() {
                return site_admin;
            }

            public void setSite_admin(boolean site_admin) {
                this.site_admin = site_admin;
            }
        }
    }
}
