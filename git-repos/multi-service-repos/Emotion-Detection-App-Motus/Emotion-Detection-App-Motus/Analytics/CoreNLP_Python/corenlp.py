import json, requests
class StanfordCoreNLP:
  
 
    def __init__(self, server_url):
        
        if server_url[-1] == '/':
            server_url = server_url[:-1]
        self.server_url = server_url
 
    def annotate(self, text, properties=None):
        assert isinstance(text, str)
        if properties is None:
            properties = {}
        else:
            assert isinstance(properties, dict)
 
     
        try:
            requests.get(self.server_url)
        except requests.exceptions.ConnectionError:
            raise Exception('Check whether you have started the CoreNLP server e.g.\n'
                            '$ cd <path_to_core_nlp_folder>/stanford-corenlp-full-2016-10-31/ \n'
                            '$ java -mx4g -cp "*" edu.stanford.nlp.pipeline.StanfordCoreNLPServer -port <port> -timeout <timeout_in_ms>')
 
        data = text.encode()
        r = requests.post(
            self.server_url, params={
                'properties': str(properties)
            }, data=data, headers={'Connection': 'close'})
        output = r.text
        if ('outputFormat' in properties
            and properties['outputFormat'] == 'json'):
            try:
                output = json.loads(output, encoding='utf-8', strict=True)
            except:
                pass
        return output
 
def sentiment_analysis_on_sentence(sentence):
    # The StanfordCoreNLP server is running on http://127.0.0.1:9000
    nlp = StanfordCoreNLP('http://127.0.0.1:9000')

    output = nlp.annotate(sentence, properties={
        "annotators": "tokenize,ssplit,parse,sentiment",
        "outputFormat": "json",
        
        "ssplit.eolonly": "true",
     
        "enforceRequirements": "false"
    })
   
    return int(output['sentences'][0]['sentimentValue'])