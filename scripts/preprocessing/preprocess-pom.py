#!/usr/bin/python

# takes as input the path of the pom.xml to be preporcessed. The outputs are two things: (1) a modified pom.xml to include all
# preproccessing needed, and (2) the original pom.xml now renamed to pom_orig.xml.

import sys
import xml.etree.ElementTree as ET
import os

#adds the serverfuzz dependency.
def add_serverfuzz_dependency(pom_name, nsmap_name):
    dependencies = pom_name.findall('.//m:dependencies', nsmap_name)
    serverfuzz_dependency = ET.SubElement(dependencies[len(dependencies) - 1], 'dependency')
    group_id = ET.SubElement(serverfuzz_dependency, 'groupId')
    group_id.text = 'edu.umn.cs'
    artifact_id = ET.SubElement(serverfuzz_dependency, 'artifactId')
    artifact_id.text = 'serverfuzz'
    version_id = ET.SubElement(serverfuzz_dependency, 'version')
    version_id.text = '1.0-SNAPSHOT'

#adds both jqf plugin and jacoco plugin
def add_plugins(pom_name, nsmap_name):
    plugins = pom_name.findall('.//m:plugins', nsmap_name)
    jqf_plugin = ET.SubElement(plugins[len(plugins) - 1], 'plugin')
    group_id = ET.SubElement(jqf_plugin, 'groupId')
    group_id.text = 'edu.berkeley.cs.jqf'
    artifact_id = ET.SubElement(jqf_plugin, 'artifactId')
    artifact_id.text = 'jqf-maven-plugin'
    version_id = ET.SubElement(jqf_plugin, 'version')
    version_id.text = '1.9'  # I probably need at some point to point this to a local jar.
    add_jacoco_plugins(pom_name, nsmap_name)

#adds jacoco plugin
def add_jacoco_plugins(pom_name, nsmap_name):
    jacoco_pom = ET.parse('jacoco.xml')
    jacoco_root = jacoco_pom.getroot()
    plugins = pom_name.find('.//m:plugins', nsmap_name)
    plugins.append(jacoco_root)


#defines the right version for different dependencies if they are used.
replace_dependency_map = {
    "aws-lambda-java-events": "3.11.0",
    "aws-java-sdk": "1.12.201",
    "aws-lambda-java-runtime-interface-client": "2.1.1",
    "aws-lambda-java-core": "1.2.1",
    "aws-java-sdk-s3": "1.12.218",
    "aws-java-sdk-textract": "1.12.253"
}


args = len(sys.argv)
if args < 1:
    print('must pass path to xml file to preprocess')
    exit(1)

pom_filename = sys.argv[1]
pom = ET.parse(pom_filename)

root = pom.getroot()

nsmap = {'m': 'http://maven.apache.org/POM/4.0.0'}
ET.register_namespace('', nsmap.get('m'))

#checks all existing dependencies and maps it with the replace_dependency_map to update them to the correct version.
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

add_serverfuzz_dependency(pom, nsmap) #adds serverfuzz dependency.
add_plugins(pom, nsmap) #adds jqf plugin and jacoco plugin

# ET.tostring(root)

#renames the original pom.xml to pom_orig.xml and generates the modified pom with the name pom.xml under the same path
os.rename(pom_filename, pom_filename.split(".")[0] + '_orig.xml')
with open(pom_filename, 'wb') as f:
    pom.write(f)
