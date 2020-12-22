#!/bin/bash

usage() {
    echo "Usage: $0 -j <path/to/depends-version.jar> -i <repo/path/> -l <java|cpp|python|kotlin|pom|ruby|xml> -o <output/path/> [-f [js|,mysql|,json|,xml|,excel|,detail|,dot|,plantuml]] [-g <package|file|method>] [-c <path/to/config.json>] [-t <date>] [-d]"

    echo "PARAMETER DESCRIPTION:"
    echo "-j jar-path                                     path to depends-x.x.x.jar"
    echo "-i input-path                                   path to repo to be analyzed"
    echo "-l language                                     project language"
    echo "-o output-path                                  output path"
    echo "-f js|,json|,xml|,excel|,detail|,dot|,plantuml  output file format"
    echo "-g package|file|method||L#                      granularity"
    echo "-c json-file                                    database configuration json"
    echo "-t date                                         analyze a specific date"
    echo "-d                                              enable parse logging"
    1>&2; exit 1;
}

if [[ $# -lt 9 || $# -gt 18 ]]; then
    usage;
fi

while getopts ":j:i:l:o:f:g:c:t:d:" args; do
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
            if [[ -z "${OPTARG}" ]]; then
                usage
            fi
            outputPath="-d ${OPTARG}"
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
            if [[ -z "${OPTARG}" ]]; then
                usage
            fi
            config="--db ${OPTARG}"
            ;;
        t)
            if [[ -z "${OPTARG}" ]]; then
                usage
            fi
            date="--date ${OPTARG}"
            ;;
        d)
            logging="-l"
            ;;
        *)
            usage
            ;;
    esac
done

# default jar
if [[ -z "${jarPath+x}" ]]; then
    jarPath=$HOME/workspaces/depends/build/distribution/depends-1.0.0.jar
fi

depends () {
    /usr/lib/jvm/jdk1.8.0_262/bin/java -jar ${jarPath} "$@";
}

# multiprocessing
tmp_fifo=/tmp/fd1
[ -e ${tmp_fifo} ] || mkfifo ${tmp_fifo}
exec 3<>${tmp_fifo}
rm -rf ${tmp_fifo}

process_num=20
for ((i=1;i<=${process_num};i++)) do
    echo >&3
done

i=0
for DIR in "${inputPath}/"*; do
    if [[ -d "${DIR}" ]]; then
        i=$(( i + 1 ))
        PROJNAME="$( basename "${DIR}" )"
        PROJPATH="$( dirname "${DIR}" )/${PROJNAME}"
        echo "[$i] Processing "${PROJPATH}" ..."

        read -u3
{
        depends ${language} ${PROJPATH} ${PROJNAME} ${outputPath} -f ${format} -g ${granularity} ${config} ${date} ${logging}
        echo >&3
} &
    fi
done

wait

exec 3<&-
exec 3>&-
