#!/usr/bin/python

import sys
import xml.etree.ElementTree as ET
import xml.dom.minidom

args = len(sys.argv)
if args < 1:
    print('must pass path to xml file to preprocess')
    exit(1)

pom = ET.parse('before/pom.xml')

# root = pom.getroot()

nsmap = {'m': 'http://maven.apache.org/POM/4.0.0'}
for mapping in pom.findall('//m:dependencies/m:dependency', nsmap):
    artifactId = mapping.find('m:artifactId', nsmap).text
    version = mapping.find('m:version', nsmap).text
    map[artifactId] = version

print(nsmap)
