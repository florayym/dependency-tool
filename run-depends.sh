#!/bin/bash

usage() {
    echo "Usage: $0 -j <path/to/depends-version.jar> -i <repo/path/> -l <java|cpp|python|kotlin|pom|ruby|xml> -o <output/path/> [-f js[,mysql|,json|,xml|,excel|,detail|,dot|,plantuml]] -g <package|file|method> [-c <path/to/config.json>]"

    echo "PARAMETER DESCRIPTION:"
    echo "-j jar-path                                     path to depends-x.x.x.jar"
    echo "-i input-path                                   path to repo to be analyzed"
    echo "-l language                                     project language"
    echo "-o output-path                                  output path"
    echo "-f js|,json|,xml|,excel|,detail|,dot|,plantuml  output file format"
    echo "-g package|file|method||L#                      granularity"
    echo "-c json-file                                    database configuration json"
    1>&2; exit 1;
}

if [[ $# -lt 10 || $# -gt 14 ]]; then
    usage;
fi

while getopts ":j:i:l:o:f:g:c:" args; do
    case "${args}" in
        j)
            jarPath=${OPTARG}
            [[ ${jarPath} =~ .*(.jar)$ ]] || usage
            ;;
        i)
            inputPath=${OPTARG}
            if [[ -z "${inputPath}" ]]; then
                usage
            fi
            ;;
        l)
            language=${OPTARG}
            [[ ${language} =~ (java|cpp|kotlin|pom|python|ruby|xml) ]] || usage
            ;;
        o)
            outputPath=${OPTARG}
            if [[ -z "${outputPath}" ]]; then
                usage
            fi
            ;;
        f)
            format=${OPTARG}
            if [[ -z "${format}" ]]; then
                format="js"
            fi
            ;;
        g)
            granularity=${OPTARG}
            if [[ -z "${granularity}" ]]; then
                granularity="package"
            fi
            [[ ${granularity} =~ (package|file|method) || ${granularity} =~ L[0-9] ]] || usage
            ;;
        c)
            config=${OPTARG}
            if [[ -z "${config}" ]]; then
                usage
            fi
            ;;
        *)
            usage
            ;;
    esac
done

JAVA_HOME=/usr/lib/jvm/jdk1.8.0_262
JAVAPATH=${JAVA_HOME}/bin/java

i=0
for DIR in "${inputPath}/"*; do
    if [[ -d "${DIR}" ]]; then
        i=$(( i + 1 ))
        PROJNAME="$( basename "${DIR}" )"
        PROJPATH="$( dirname "${DIR}" )/${PROJNAME}"
        echo "[$i] Processing "${PROJPATH}" ..."
        ${JAVAPATH} -jar ${jarPath} ${language} ${PROJPATH} ${PROJNAME} -d ${outputPath} -f ${format} -g ${granularity} --db ${config}
    fi
done

echo "Successfully batch updated dependency analysis graphs!"
