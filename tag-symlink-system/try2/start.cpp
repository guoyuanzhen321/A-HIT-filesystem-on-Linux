#include "start.h"
#include "ui_start.h"
#include "widget.h"

start::start(QWidget *parent) :
    QDialog(parent),
    ui(new Ui::start)
{
    ui->setupUi(this);
}

start::~start()
{
    delete ui;
}

void start::on_Button_open_clicked()
{
    Widget *w=new Widget();
    w->show();
    QDialog::close();
}

void start::on_Button_close_clicked()
{
    QDialog::close();
}
