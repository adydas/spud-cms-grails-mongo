package spud.cms

import spud.core.*

class LayoutParserService {
	def grailsApplication
	def spudLayoutService

	def layoutsForSite(siteId=0,parentLayout=null) {
		def layoutService  = spudLayoutService.layoutServiceForSite(siteId)
		def layouts        = layoutService.layoutsForSite(siteId)
		def defaultLayout  = grailsApplication.config.spud.cms.defaultLayout ?: 'application'
		def layoutMetaList = []

		if(grailsApplication.warDeployed && layoutService.dynamicOnWarDeploy != true) {
			// Use Config For Fetching Layout Information
			return grailsApplication.config.spud.cms.layouts
		} else {
			// Scan Layout Files Dynamically
			layouts.each { layout ->
				def contents = layoutService.layoutContents(layout)
				def meta = metaInfoForLayoutContents(layout, contents)
				if(meta) {
					layoutMetaList << meta + [layout: layout]
				} else if (layout == defaultLayout) {
					layoutMetaList << [layout: layout, name: layout.humanize().titlecase(), html: ['Body']]
				}
			}
			return layoutMetaList
		}

	}

	private metaInfoForLayoutContents(layout, contents) {
		if(!contents) {
			return null
		}
		def lines = contents.split("\n")
		def layoutMeta = [layout: layout, name: layout.humanize().titlecase(), html: [], partials: [], sites:[]]
		def metaFound = false
		lines.each { line ->
			def directive = directiveForLine(line)
			if(directive) {
				metaFound = true
				switch(directive.command) {
					case 'template_name':
						def templateName = directive.args.join(" ").trim()
						if(templateName) {
							layoutMeta.name = templateName.humanize().titlecase()
						}
						break
					case 'html':
						def htmlName = directive.args.join(" ").trim()
						if(htmlName) {
							layoutMeta.html << htmlName.humanize().titlecase()
						}
						break
					case 'site':
						directive.args.each { arg ->
							siteName = arg.trim()
							if(siteName) {
								layoutMeta.sites << siteName
							}
						}
						break
					case 'partial':
						def partialName = directive.args.join(" ").trim()
						if(partialName) {
							layoutMeta.partials << partialName
						}
						break
				}
			}
		}
		if(metaFound) {
			return layoutMeta
		}
		return null
	}

	private directiveForLine(line) {
		def directive = line.find(/\/\/=(.*)/) { fullMatch, directive -> return directive }?.trim()

		if(directive) {
      def argsWithDirective = directive.split(" ")
      def command = argsWithDirective[0].toLowerCase()
      def args = argsWithDirective[1..argsWithDirective.size()-1]
      return [command: command, args:args]
		}
		return null
	}
}


// spud.core.layouts = [
//     [name: 'page',partials:
//         [
//             body: [formats: ['html']],
//             sidebar: [formats: ['html']]
//         ]
//     ],
//     [name: 'home', partials:
//         [
//             body: [formats: ['html']]
//         ]
//     , siteId: 0]
// ]
