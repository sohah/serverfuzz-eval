# import requests
#
#
# #request_content = 'https://api.github.com/search?q=com.amazonaws.services.lambda.runtime.events.S3Event&sort=starts&access_token=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'
#
# request_content = 'curl -H \'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\' https://api.github.com/search?q=com.amazonaws.services.lambda.runtime.events.S3Event'
#
# public_repos = requests.get(request_content).json()
#
# print(f'the request result is {public_repos}')
#
#
#

import subprocess
import json
import time


def run_gitclone(repo):
    print(f'clonning github repo {repo}')
    repo = '/'.join(repo.split('/')[3:])
    clone_command = f'git -C ../git-repos/round2 clone git@github.com:{repo}'
    subprocess.run(clone_command, shell=True, text=True)

def run_maven(repo_path):
    mvn_command = f'mvn -f {repo_path}/pom.xml install'
    mvn_output = subprocess.run(mvn_command, shell=True, text=True)
    print(mvn_output)


def obtain_repo(starred_repo):
    print('---------------- cloning and compiling starred repos ---------------- ')
    print(starred_repo)
    run_gitclone(starred_repo)
    directory_name = starred_repo.split('/')[-1]
    repo_path = f'../git-repos/round2/{directory_name}'
    run_maven(repo_path)

##### begin of configuration to set for the script to run######

wait_time = 30 # seconds
wait_factor = 2 # the factor of increasing wait if we received an error in a request.
addition_factor = 600 # constant wait of 10 minutes added when the wait time reaches one hour
page = 1 #iteration of pagination
pagination_limit = 26
stars_limit = 5
##### end of configuration to set for the script to run######

repos_with_stars = []
counter = 0
f = open('starred_repos_round2.txt', 'w')
f.write('starred_repo, #stars')

while page <= pagination_limit:
    print(f'----------------  trying page #{page}---------------- ')
    command_str = f'gh api --method=GET "search/code?q=com.amazonaws.services.lambda.runtime.events.S3Event&access_token=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&page={str(page)}&per_page=100\"'
    output = subprocess.run(command_str, capture_output=True, shell=True, text=True)
    if len(output.stderr) > 0: #non-empty error occurred; wait longer
        if wait_time < 3600:  # when wait time is less than an hour multiply by wait_factor otherwise, use the constant_factor
            wait_time *= wait_factor
        else:
            wait_time +=addition_factor
        print(f'github ban encountered, increasing waiting time. Wait time now = {wait_time}')
        time.sleep(wait_time)
    else:
        page += 1
        result = json.loads(output.stdout)
        item_str = 'items'
        print(f'printing first item in the output for comparison\n{result[item_str][0]}')
        repo_index = 0
        items_len = len(result['items'])
        while repo_index < items_len:
            item = result['items'][repo_index]
            if '.java' not in item['name']:
                counter += 1
                repo_index += 1
                continue
            github_repo = item['repository']['url']
            github_stars_url = item['repository']['stargazers_url']
            if 'sohah' in github_stars_url: # skipping my repo
                print('skipping my repo')
                counter += 1
                repo_index += 1
                continue
            github_stars_url = 'gh api ' + github_stars_url.replace('https://api.github.com/','') #removing the header
            output = subprocess.run(github_stars_url, capture_output=True, shell=True, text=True)
            if len(output.stderr) > 0:  # non-empty error occurred, wait longer
                if wait_time < 3600:  # when wait time is less than an hour multiply by wait_factor otherwise, use the constant_factor
                    wait_time *= wait_factor
                else:
                    wait_time += addition_factor
                print(f'github ban encountered, increasing waiting time. Wait time now = {wait_time}')
                time.sleep(wait_time)
            else:
                counter += 1
                repo_index += 1
                num_of_stars = len(json.loads(output.stdout or '{}'))
                if num_of_stars >= stars_limit:
                    print(f'item collected of interest:{item}')
                    https_repo_name = item['repository']['html_url']
                    if https_repo_name not in repos_with_stars:  # do not allow duplicate clones
                        print(f'found interesting repo:{https_repo_name} with stars={num_of_stars} for item:{item}')
                        f.write(f'{https_repo_name}, {num_of_stars}\n')
                        repos_with_stars.append(https_repo_name)
                        obtain_repo(https_repo_name)

f.close()

# print('---------------- cloning and compiling starred repos ---------------- ')
# for starred_repo in repos_with_stars:
#     print(starred_repo)
#     run_gitclone(starred_repo)
#     directory_name = starred_repo.split('/')[-1]
#     repo_path = f'../git-repos/round2/{directory_name}'
#     run_maven(repo_path)
#
