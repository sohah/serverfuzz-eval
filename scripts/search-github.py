#!/usr/bin/env python3

import subprocess
from pathlib import Path
import shutil



CRITERIA='aws lambda s3'
LIMIT=30
SUPPORTED_EVENTS=['aws', 'com.amazonaws.services.lambda.runtime.events.S3Event', 'com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification']

##assumes the existance of folder structure where ../git-repos folder exists relative to this script.

def find_files(root, extensions):
    for ext in extensions:
        yield from Path(root).glob(f'**/*.{ext}')

def run_maven(repo_path):
    mvn_command = f'mvn -f {repo_path}/pom.xml install'
    mvn_output = subprocess.run(mvn_command, shell=True, text=True)
    print(mvn_output)


def run_gitclone(repo, count):
    print(f'print repo # {count,repo}')
    print(f'clonning github repo {repo}')
    clone_command = f'git -C ../git-repos clone git@github.com:{repo}'
    subprocess.run(clone_command, shell=True, text=True)


def run_git_search():
    gh_command='gh search repos --language=java --limit %s \' %s\''%(LIMIT, CRITERIA)
    repos = subprocess.run(gh_command, capture_output=True, shell=True, text=True)
    return repos

def remove_repo(path):
    shutil.rmtree(path, ignore_errors=False, onerror=None)


def search_file_for_supported_aws_events(java_file):
    with open(java_file) as f:
        code_str = f.readline()
        # print(f'printing file content {code_str}')
        for event in SUPPORTED_EVENTS:
            print(f'event={event}')
            if event in code_str:
                return True
    return False

def check_repo_contains_supported_event(repo_path):
    #search for java files, compile if found
    for java_file in find_files(repo_path, ['java']):
        has_aws_event = search_file_for_supported_aws_events(java_file)

        if(has_aws_event):
            print(f'{java_file} has the event we are looking for.')
            return True

    print(f'{repo_path} does not contain a supported event')
    return False

def main():
    repos = run_git_search()
    count = 0
    # print('printing results\n')#, repos.stdout)
    for repo in repos.stdout.split('\n'): #process every line seperately
        count +=1
        repo = repo.split('\t')[0]  #extract only the url
        if(len(repo) > 0):

            run_gitclone(repo, count)
            directory_name = repo.split('/')[1]
            repo_path = f'../git-repos/{directory_name}'

            # print(f'directory_name is = {directory_name}')

            repo_has_supported_event = check_repo_contains_supported_event(repo_path)
            if(repo_has_supported_event):
                run_maven(repo_path)
            else:
                remove_repo(repo_path)


main()
