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

counter=0
wait_time = 30 #seconds
wait_factor = 2 # the factor of increasing wait if we received an error in a request.
i = 1 #iteration of pagination
pagination_limit = 10

while i<pagination_limit:
    command_str =f'gh api --method=GET "search/code?q=com.amazonaws.services.lambda.runtime.events.S3Event&access_token=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&page={str(i)}&per_page=100\"'
    output = subprocess.run(command_str, capture_output=True, shell=True, text=True)
    if len(output.stderr) > 0: #non empty error occured, wait longer
        wait_time *= wait_factor
        print(f'github ban encountered, increasing waiting time. Wait time now = {wait_time}')
    else:
        i +=1
        result = json.loads(output.stdout)
        item_str = 'items'
        print(f'printing first item in the output for comparison\n{result[item_str][0]}')
        for item in result['items']:
            counter +=1
            github_repo = item['repository']['url']
            github_stars_url = item['repository']['stargazers_url']
            output = subprocess.run(github_stars_url, capture_output=True, shell=True, text=True)
            num_of_stars = len(json.loads(output.stdout or '{}'))
            if num_of_stars > 0:
                print(f'found interesting repo{github_repo} with stars={num_of_stars}')
        print(f'printing count after {i}th GET request = {counter}')
    time.sleep(wait_time)

