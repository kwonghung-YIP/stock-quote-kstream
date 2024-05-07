{{- define "k8s.subdomain" -}}
{{- printf "%s.svc.%s" $.Release.Namespace $.Values.kubernetes.cluster -}}
{{- end -}}

{{/*
Create the "controller.quorum.voters" list
*/}}
{{- define "controller.quorum.voters" -}}
{{- $start := $.Values.controller.ordinals.start | int -}}
{{- $end := $.Values.controller.replicas | int -}}
{{- $port := $.Values.controller.port | toString -}}
{{- $service := $.Values.controller.service.name -}}
{{- $node := $.Values.controller.statefulset.name -}}
{{- $controllerList := list -}}
{{- range $id := seq $start $end | splitList " " -}}
{{- $host := printf "%s-%s.%s.%s" $node $id $service (include "k8s.subdomain" $) -}}
{{- $controller := printf "%s@%s:%s" $id $host $port -}}
{{- $controllerList = append $controllerList $controller -}}
{{- end -}}
{{- printf "%s" (join "," $controllerList)}}
{{- end -}}

{{/*
Create the "bootstrap.servers" list
*/}}
{{- define "bootstrap.servers" -}}
{{- $start := $.Values.broker.ordinals.start | int -}}
{{- $end := add $start (int $.Values.broker.replicas) -1 | int -}}
{{- $port := $.Values.broker.port | toString -}}
{{- $service := $.Values.broker.service.name -}}
{{- $node := $.Values.broker.statefulset.name -}}
{{- $serverList := list -}}
{{- range $id := seq $start $end | splitList " " -}}
{{- $host := printf "%s-%s.%s.%s" $node $id $service (include "k8s.subdomain" $) -}}
{{- $bootstrap := printf "%s:%s" $host $port -}}
{{- $serverList = append $serverList $bootstrap -}}
{{- end -}}
{{- printf "%s" (join "," $serverList)}}
{{- end -}}
