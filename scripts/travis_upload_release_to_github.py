#!/usr/bin/env python2
# -*- coding: utf-8 -*-
from __future__ import print_function

import os
import sys
import httplib
import urllib
import urlparse
import json
import fnmatch
import re
from os import getenv
from subprocess import check_output
from subprocess import CalledProcessError

__author__ = 'mariotaku'
git_https_url_prefix = 'https://github.com/'
git_ssh_url_prefix = 'git@github.com:'
git_git_url_prefix = 'git://github.com/'
git_file_suffix = '.git'
github_header_accept = 'application/vnd.github.v3+json'
github_header_user_agent = 'TravisUploader/0.1'

DEVNULL = open(os.devnull, 'w')
repo_url = None

try:
    repo_url = check_output(['git', 'config', '--get', 'remote.origin.url']).splitlines()[0]
except CalledProcessError:
    print('No remote url for this project, abort')
    exit(0)

user_repo_name = None
if repo_url.startswith(git_ssh_url_prefix):
    user_repo_name = repo_url[len(git_ssh_url_prefix):]
elif repo_url.startswith(git_https_url_prefix):
    user_repo_name = repo_url[len(git_https_url_prefix):]
elif repo_url.startswith(git_git_url_prefix):
    user_repo_name = repo_url[len(git_git_url_prefix):]

if not user_repo_name:
    print('Not a github repo (%s), abort' % repo_url, file=sys.stderr)
    exit(0)

if user_repo_name.endswith(git_file_suffix):
    user_repo_name = user_repo_name[:-len(git_file_suffix)]

current_tag = None
current_tag_body = None
try:
    current_tag = check_output(['git', 'describe', '--tags', '--exact-match', '--abbrev=0'],
                               stderr=DEVNULL).splitlines()[0]
except CalledProcessError:
    print('This commit doesn\'t have tag, abort', file=sys.stderr)
    exit(0)
try:
    current_tag_body = '\n'.join(
        check_output(['git', 'show', '-s', '--format=%b', current_tag], stderr=DEVNULL).splitlines()[2:])
except CalledProcessError:
    current_tag_body = "Automatic upload for version %s" % current_tag

github_access_token = getenv('GITHUB_ACCESS_TOKEN')

if not github_access_token:
    print('No access token given, abort', file=sys.stderr)
    exit(0)

github_authorization_header = "token %s" % github_access_token

print('Creating release for tag %s' % current_tag)

req_headers = {'Accept': github_header_accept}

conn = httplib.HTTPSConnection('api.github.com')
conn.request('POST', '/repos/%s/releases' % user_repo_name,
             body=json.dumps({
                 'tag_name': current_tag,
                 'name': "Version %s" % current_tag,
                 'body': current_tag_body
             }),
             headers={
                 'Accept': github_header_accept,
                 'Authorization': github_authorization_header,
                 'Content-Type': 'application/json',
                 'User-Agent': github_header_user_agent
             })
response = conn.getresponse()
if response.status == 422:
    conn = httplib.HTTPSConnection('api.github.com')
    conn.request('GET', '/repos/%s/releases/tags/%s' % (user_repo_name, current_tag),
                 headers={
                     'Accept': github_header_accept,
                     'Authorization': github_authorization_header,
                     'User-Agent': github_header_user_agent
                 })
    response = conn.getresponse()

if response.status not in range(200, 204):
    print('Unable to create or get release, abort', file=sys.stderr)
    exit(0)

response_values = json.loads(response.read())

upload_url = urlparse.urlparse(re.sub('\{\?([\w\d_\-]+)\}', '', response_values['upload_url']))
for root, dirnames, filenames in os.walk(os.getcwd()):
    for filename in fnmatch.filter(filenames, '*-release.apk'):
        conn = httplib.HTTPSConnection(upload_url.hostname)
        conn.request('POST', "%s?%s" % (upload_url.path, urllib.urlencode({'name': filename})),
                     body=open(os.path.join(root, filename), 'r'),
                     headers={
                         'Accept': github_header_accept,
                         'Authorization': github_authorization_header,
                         'Content-Type': 'application/json',
                         'User-Agent': github_header_user_agent
                     })
        print("Upload %s returned %d" % (filename, conn.getresponse().status))