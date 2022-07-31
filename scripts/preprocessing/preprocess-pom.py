#!/usr/bin/python

import sys
import xml.etree.ElementTree as ET
from bs4 import BeautifulSoup

replace_dependency_map = {

    "aws-lambda-java-events": "3.11.0",
    "aws-java-sdk": "1.12.201",
    "aws-lambda-java-runtime-interface-client": "2.1.1",
    "aws-lambda-java-core": "1.2.1",
    "aws-java-sdk-s3": "1.12.218",
    "aws-java-sdk-textract": "1.12.253"
}

# def prettify(elem):
#     """Return a pretty-printed XML string for the Element.
#     """
#     rough_string = ET.tostring(elem, 'utf-8')
#     reparsed = minidom.parseString(rough_string)
#     return reparsed.toprettyxml(indent="\t")

args = len(sys.argv)
if args < 1:
    print('must pass path to xml file to preprocess')
    exit(1)

pom = ET.parse('before/pom.xml')

root = pom.getroot()

nsmap = {'m': 'http://maven.apache.org/POM/4.0.0'}
ET.register_namespace('', nsmap.get('m'))

for mapping in pom.findall('.//m:dependencies/m:dependency', nsmap):
    artifactId = mapping.find('m:artifactId', nsmap).text
    version = mapping.find('m:version', nsmap)

    if artifactId in replace_dependency_map:
        new_version = replace_dependency_map.get(artifactId)
        if version is None:
            new_element = ET.SubElement(mapping, 'version')
            new_element.text = new_version
        else:
            version.text = new_version

dependencies = pom.findall('.//m:dependencies', nsmap)

# serverfuzz_dependency = ET.Element('dependency')
serverfuzz_dependency = ET.SubElement(dependencies[len(dependencies) - 1], 'dependency')
group_id = ET.SubElement(serverfuzz_dependency, 'groupId')
group_id.text = 'edu.umn.cs'
artifactId = ET.SubElement(serverfuzz_dependency, 'artifactId')
artifactId.text = 'serverfuzz'
version = ET.SubElement(serverfuzz_dependency, 'version')
version.text = '1.0-SNAPSHOT'

# ET.tostring(root)

with open('after/pom.xml', 'wb') as f:
    pom.write(f)

#
# print('printing new map:')
# for mapping in pom.findall('.//m:dependencies/m:dependency', nsmap):
#     artifactId = mapping.find('m:artifactId', nsmap).text
#     version = mapping.find('m:version', nsmap)
#     if version is None:
#         print(f'artifact:{artifactId}, version:{None}')
#     else:
#         print(f'artifact:{artifactId}, version:{version.text}')
