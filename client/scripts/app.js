function app() {
   const token = localStorage.getItem('userToken');
   if (!token) {
      window.location.href = 'login.html';
      return;
   }

   $.ajaxSetup({
      beforeSend: function (xhr) {
         xhr.setRequestHeader('Authorization', 'Basic ' + token);
      }
   });

   loadGroups();
   loadProducts();

   $('#search-btn').on('click', function () {
      const query = $('#search-input').val();
      loadProducts(query);
   });

   $('#add-group').on('click', function () {
      $('#groupModal').modal('show');
   });

   $('#save-group-btn').on('click', function () {
      const name = $('#group-name').val().trim();
      const desc = $('#group-description').val().trim();
      if (!name) {
         alert('Введіть назву групи');
         return;
      }

      $.ajax({
         url: 'http://localhost:8211/groups',
         method: 'POST',
         contentType: 'application/json',
         data: JSON.stringify({ name, description: desc }),
         success: function () {
            $('#groupModal').modal('hide');
            $('#group-name').val('');
            $('#group-description').val('');
            loadGroups();
         },
         error: function () {
            alert('Не вдалося додати групу (можливо, така назва вже існує)');
         }
      });
   });

   $('#add-product').on('click', function () {
      $('#productModal').modal('show');
      loadGroupOptions();
   });

   $('#save-product-btn').on('click', function () {
      const name = $('#product-name').val().trim();
      const desc = $('#product-description').val().trim();
      const manufacturer = $('#product-manufacturer').val().trim();
      const quantity = parseInt($('#product-quantity').val());
      const price = parseFloat($('#product-price').val());
      const group = $('#product-group').val();

      if (!name || isNaN(quantity) || isNaN(price) || !group) {
         alert('Будь ласка, заповніть всі поля правильно');
         return;
      }

      if (quantity < 0 || price < 0) {
         alert('Кількість та ціна не можуть бути від\'ємними');
         return;
      }

      $.ajax({
         url: 'http://localhost:8211/products',
         method: 'POST',
         contentType: 'application/json',
         data: JSON.stringify({
            name,
            description: desc,
            manufacturer,
            quantity,
            price,
            group
         }),
         success: function () {
            $('#productModal').modal('hide');
            loadProducts();
         },
         error: function () {
            alert('Не вдалося додати товар');
         }
      });
   });

   $('#logout').on('click', function(e) {
      e.preventDefault();

      localStorage.removeItem('userToken');
      location.reload(); 
   })
}

function loadGroupOptions() {
   $.get('http://localhost:8211/groups', function (groups) {
      const select = $('#product-group');
      select.empty();
      groups.forEach(group => {
         select.append(`<option value="${group.name}">${group.name}</option>`);
      });
   });
}

function loadGroups() {
   $.get('http://localhost:8211/groups', function (groups) {
      const list = $('#group-list');
      list.empty();
      groups.forEach(group => {
          const item = $(
            `<li class="d-flex align-items-center mt-2">
               <a href="#" class="btn btn-secondary group-link flex-fill text-start" data-name="${group.name}" style="flex:1 1 auto;">${group.name}</a>
               <button class="btn text-white btn-sm btn-danger delete-group ms-2" data-name="${group.name}">✘</button> 
            </li>`
          );
         item.find('a').on('click', () => loadProducts('', group.name));
         item.find('.delete-group').on('click', function () {
            const name = $(this).data('name');
            deleteGroup(name);
         });
         list.append(item);
      });
   });
}

function loadProducts(search = '', groupFilter = '') {
   const url = 'http://localhost:8211/products' + (search ? '?search=' + encodeURIComponent(search) : '');

   $.get(url, function (products) {
      const body = $('#product-table-body');
      body.empty();
      let total = 0;

      products.forEach(product => {
         if (groupFilter && product.group !== groupFilter) return;

         const sum = product.quantity * product.price;
         total += sum;
         const row = $(`
           <tr>
             <td>${product.name}</td>
             <td>${product.description}</td>
             <td>${product.manufacturer}</td>
             <td>${product.quantity}</td>
             <td>${product.price.toFixed(2)}</td>
             <td>${sum.toFixed(2)}</td>
             <td><button class="btn btn-sm btn-danger delete-product" data-name="${product.name}">✘</button></td>
           </tr>`);
         row.find('.delete-product').on('click', () => deleteProduct(product.name));
         body.append(row);
      });

      $('#total-value').text(total.toFixed(2) + ' грн');
   });
}

function deleteGroup(name) {
   if (!confirm(`Видалити групу ${name}? Всі товари в ній також буде видалено.`)) return;
   $.ajax({
      url: 'http://localhost:8211/groups/' + encodeURIComponent(name),
      method: 'DELETE',
      success: function () {
         loadGroups();
         loadProducts(); // reload everything after delete
      },
      error: function () {
         alert('Не вдалося видалити групу');
      }
   });
}

function deleteProduct(name) {
   if (!confirm(`Видалити товар ${name}?`)) return;
   $.ajax({
      url: 'http://localhost:8211/products/' + encodeURIComponent(name),
      method: 'DELETE',
      success: function () {
         loadProducts();
      },
      error: function () {
         alert('Не вдалося видалити товар');
      }
   });
}