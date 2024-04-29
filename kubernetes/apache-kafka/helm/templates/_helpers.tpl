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