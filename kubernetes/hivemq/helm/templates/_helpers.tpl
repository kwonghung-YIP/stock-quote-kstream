{{- define "k8s.cluster.name" -}}
{{- printf "%s.svc.%s" $.Release.Namespace $.Values.kubernetes.cluster -}}
{{- end -}}