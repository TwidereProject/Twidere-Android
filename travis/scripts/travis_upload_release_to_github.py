#!/usr/bin/env python2
# -*- coding: utf-8 -*-
from __future__ import print_function

import os
import sys
import urllib2
import json
import fnmatch
import magic
import uritemplate
import string
from os import getenv
from subprocess import check_output
from subprocess import CalledProcessError
from urllib2 import HTTPError

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

github_user_name = string.split(user_repo_name, '/')[0]
github_repo_name = string.split(user_repo_name, '/')[1]

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

req_headers = {'Accept': github_header_accept}

request = urllib2.Request(
    uritemplate.expand('https://api.github.com/repos/{user}/{repo}/releases/tags/{tag}',
                       {'user': github_user_name, 'repo': github_repo_name, 'tag': current_tag}),
    headers={
        'Accept': github_header_accept,
        'Authorization': github_authorization_header,
        'User-Agent': github_header_user_agent
    })
response = None
try:
    response = urllib2.urlopen(request)
except HTTPError, err:
    if err.code == 404:
        print('Creating release for tag %s' % current_tag)
        request = urllib2.Request(
            uritemplate.expand('https://api.github.com/repos/{user}/{repo}/releases',
                               {'user': github_user_name, 'repo': github_repo_name}),
            data=json.dumps({
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
        try:
            response = urllib2.urlopen(request)
        except HTTPError:
            print('Unable to create release, abort', file=sys.stderr)
            exit(0)
    else:
        response = None

if not response:
    print('Unable to get release, abort', file=sys.stderr)
    exit(0)

response_values = json.loads(response.read())

for root, dirnames, filenames in os.walk(os.getcwd()):
    for filename in fnmatch.filter(filenames, '*-release.apk'):
        file_path = os.path.join(root, filename)
        request = urllib2.Request(
            uritemplate.expand(response_values['upload_url'], {'name': filename}),
            data=open(file_path, 'rb'),
            headers={
                'Accept': github_header_accept,
                'Authorization': github_authorization_header,
                'Content-Type': magic.from_file(file_path, mime=True),
                'Content-Length': os.path.getsize(file_path),
                'User-Agent': github_header_user_agent
            })
        print("Uploading %s ..." % filename),
        try:
            response = urllib2.urlopen(request)
            print("OK")
        except HTTPError, err:
            print("Error %d" % err.code)
