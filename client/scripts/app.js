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
      $('#groupModalLabel').text('Додати групу товарів');
      $('#group-name').val('');
      $('#group-description').val('');
      $('#groupModal').data('mode', 'add').modal('show');
   });

   $('#save-group-btn').on('click', function () {
      const name = $('#group-name').val().trim();
      const desc = $('#group-description').val().trim();
      if (!name) {
         alert('Введіть назву групи');
         return;
      }
      const mode = $('#groupModal').data('mode');
      const method = mode === 'edit' ? 'PUT' : 'POST';

      $.ajax({
         url: 'http://localhost:8211/groups',
         method: method,
         contentType: 'application/json',
         data: JSON.stringify({ name, description: desc }),
         success: function () {
            $('#groupModal').modal('hide');
            $('#group-name').val('');
            $('#group-description').val('');
            loadGroups();
         },
         error: function () {
            alert('Не вдалося зберегти групу (можливо, така назва вже існує)');
         }
      });
   });

   $('#add-product').on('click', function () {
      $('#productModalLabel').text('Додати товар');
      $('#productModal').data('mode', 'add');
      $('#product-name').prop('readonly', false).val('');
      $('#product-description').val('');
      $('#product-manufacturer').val('');
      $('#product-quantity').val('');
      $('#product-price').val('');
      $('#product-group').val('');
      loadGroupOptions();
      $('#productModal').modal('show');
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

      const mode = $('#productModal').data('mode');
      const url = mode === 'edit' ? `http://localhost:8211/products/${encodeURIComponent(name)}` : 'http://localhost:8211/products';
      const method = mode === 'edit' ? 'PUT' : 'POST';

      const payload = {
         name,
         description: desc,
         manufacturer,
         quantity,
         price,
         group
      };
      if (mode === 'edit') delete payload.name;

      $.ajax({
         url: url,
         method: method,
         contentType: 'application/json',
         data: JSON.stringify(payload),
         success: function () {
            $('#productModal').modal('hide');
            loadProducts();
         },
         error: function () {
            alert('Не вдалося зберегти товар');
         }
      });
   });

   $('#restock-product').on('click', function () {
      const productName = prompt('Введіть назву товару, який потрібно прийняти на склад:');
      if (!productName) return;
      const addQuantity = parseInt(prompt('Кількість товару для прийому:'));
      if (isNaN(addQuantity) || addQuantity <= 0) return alert('Некоректна кількість');

      $.get('http://localhost:8211/products?search=' + encodeURIComponent(productName), function (products) {
         const product = products.find(p => p.name === productName);
         if (!product) return alert('Товар не знайдено');

         const payload = {
            description: product.description,
            manufacturer: product.manufacturer,
            quantity: product.quantity,
            price: product.price,
            group: product.group,
         }

         payload.quantity += addQuantity;
         $.ajax({
            url: `http://localhost:8211/products/${encodeURIComponent(product.name)}`,
            method: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify(payload),
            success: function () {
               loadProducts();
            },
            error: function (err) {
               alert('Не вдалося оновити товар:'+JSON.stringify(err));
            }
         });
      });
   });

   $('#sell-product').on('click', function () {
      const productName = prompt('Введіть назву товару, який потрібно списати:');
      if (!productName) return;
      const removeQuantity = parseInt(prompt('Кількість товару для списання:'));
      if (isNaN(removeQuantity) || removeQuantity <= 0) return alert('Некоректна кількість');

      $.get('http://localhost:8211/products?search=' + encodeURIComponent(productName), function (products) {
         const product = products.find(p => p.name === productName);
         if (!product) return alert('Товар не знайдено');
         if (product.quantity < removeQuantity) return alert('Недостатньо товару на складі');

         const payload = {
            description: product.description,
            manufacturer: product.manufacturer,
            quantity: product.quantity,
            price: product.price,
            group: product.group,
         }

         payload.quantity -= removeQuantity;
         $.ajax({
            url: `http://localhost:8211/products/${encodeURIComponent(product.name)}`,
            method: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify(payload),
            success: function () {
               loadProducts();
            },
            error: function () {
               alert('Не вдалося оновити товар');
            }
         });
      });
   });

   $('#logout').on('click', function(e) {
      e.preventDefault();

      localStorage.removeItem('userToken');
      location.reload(); 
   })
}

function loadGroups() {
   $.get('http://localhost:8211/groups', function (groups) {
      const list = $('#group-list');
      list.empty();
      groups.forEach(group => {
         const item = $(`
            <li class="nav-item d-flex justify-content-between align-items-center">
               <a href="#" class="nav-link group-link flex-grow-1" data-name="${group.name}" data-description="${group.description}">${group.name}</a>
               <div>
                 <button class="btn btn-sm btn-secondary edit-group" data-name="${group.name}" data-description="${group.description}">✎</button>
                 <button class="btn text-white btn-sm btn-danger delete-group ms-1" data-name="${group.name}">✘</button>
               </div>
             </li>`);
         item.find('a.group-link').on('click', () => loadProducts('', group.name));
         item.find('.delete-group').on('click', function () {
            const name = $(this).data('name');
            deleteGroup(name);
         });
         item.find('.edit-group').on('click', function () {
            $('#groupModalLabel').text('Редагувати групу');
            $('#group-name').val($(this).data('name')).prop('readonly', true);
            $('#group-description').val($(this).data('description'));
            $('#groupModal').data('mode', 'edit').modal('show');
         });
         list.append(item);
      });
   });
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

function deleteGroup(name) {
   if (!confirm(`Видалити групу ${name}? Всі товари в ній також буде видалено.`)) return;
   $.ajax({
      url: 'http://localhost:8211/groups/' + encodeURIComponent(name),
      method: 'DELETE',
      success: function () {
         loadGroups();
         loadProducts();
      },
      error: function () {
         alert('Не вдалося видалити групу');
      }
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
             <td>
               <button class="btn btn-sm btn-secondary edit-product" data-name="${product.name}">✎</button>
               <button class="btn btn-sm text-white btn-danger delete-product" data-name="${product.name}">✘</button>
             </td>
           </tr>`);
         row.find('.delete-product').on('click', () => deleteProduct(product.name));
         row.find('.edit-product').on('click', () => {
            $('#productModalLabel').text('Редагувати товар');
            $('#productModal').data('mode', 'edit');
            $('#product-name').val(product.name).prop('readonly', true);
            $('#product-description').val(product.description);
            $('#product-manufacturer').val(product.manufacturer);
            $('#product-quantity').val(product.quantity);
            $('#product-price').val(product.price);
            loadGroupOptions();
            setTimeout(() => $('#product-group').val(product.group), 100);
            $('#productModal').modal('show');
         });
         body.append(row);
      });

      $('#total-value').text(total.toFixed(2) + ' грн');
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