<g:applyLayout name="spud/admin/detail" >

<content tag="data_controls">
  <spAdmin:link action="create" title="New Page" class="btn btn-primary">New Page</spAdmin:link>
</content>
<content tag="detail">
  <div class="page_list">
    <g:each var="page" in="${pages}">
      <g:render template="/spud/admin/pages/page_row" model="[page: page]"/>
    </g:each>
  </div>
</content>

</g:applyLayout>


