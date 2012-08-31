#include <QtGui/QApplication>
#include "widget.h"
#include "start.h"

int main(int argc, char *argv[])
{
    QApplication a(argc, argv);
   // Widget w;
    //w.show();
    start w;
  //  Widget x;
    w.show();
   // QObject::connect(w.Button_open,SIGNAL(clicked()),&x,SLOT(show()));

    return a.exec();
}
