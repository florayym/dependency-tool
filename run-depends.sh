#!/bin/bash

usage() {
    echo "Usage: $0 -j <path/to/depends-version.jar> -i <repo/path/> -l <java|cpp|python|xml|kotlin> -o <output/path/> [-f <js(default)|json|xml|excel|detail|dot|plantuml>] -g <file|method|package> [-c <path/to/config.json>]"

    echo "PARAMETER DESCRIPTION:"
    echo "-j jar-path                                        path to depends-x.x.x.jar"
    echo "-i input-path                                      path to repo to be analyzed"
    echo "-l java|cpp|python|xml|kotlin                      project language"
    echo "-o output-path                                     output path"
    echo "-f js(default)|json|xml|excel|detail|dot|plantuml  output file format"
    echo "-g file(default)|method|package|L#                 granularity"
    echo "-c json-file                                       database configuration json"
    1>&2; exit 1;
}

if [[ $# -lt 10 || $# -gt 14 ]]; then
    usage;
fi

while getopts ":j:i:l:o:f:g:c:" args; do
    case "${args}" in
        j)
            jarPath=${OPTARG}
            ;;
        i)
            inputPath=${OPTARG}
            ;;
        l)
            language=${OPTARG}
            ;;
        o)
            outputPath=${OPTARG}
            ;;
        f)
            format=${OPTARG}
            if [[ -z "${format}" ]]; then
                format="js"
            fi
            ((format == js || format == json || format == xml || format == excel || format == detail || format == dot || format == plantuml)) || usage
            ;;
        g)
            granularity=${OPTARG}
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

for DIR in "${inputPath}/"*; do
    if [[ -d "${DIR}" ]]; then
        PROJNAME="$( basename "${DIR}" )"
        PROJPATH="$( dirname "${DIR}" )/${PROJNAME}"
        echo "Processing "${PROJPATH}" ..."
        eval "java -jar ${jarPath} ${language} ${PROJPATH} ${PROJNAME} -d ${outputPath} -f ${format} -g ${granularity} --db ${config}"
    fi
done

echo "Successfully batch updated dependency analysis graphs!"
